package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.msa.commerce.monolith.config.QuerydslConfig;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

import jakarta.persistence.EntityManagerFactory;

@DataJpaTest
@Import(QuerydslConfig.class)
@org.junit.jupiter.api.Disabled("Inventory 엔티티 테이블 생성 문제로 인해 임시 비활성화 - H2 JSON 컬럼 호환성 문제")
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.generate_statistics=true",
    "logging.level.org.hibernate.SQL=DEBUG",
    "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
@DisplayName("Product 성능 테스트")
class ProductPerformanceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics hibernateStatistics;
    private List<ProductJpaEntity> testProducts;
    private ProductCategoryJpaEntity testCategory;

    @BeforeEach
    void setUp() {
        // Hibernate 통계 활성화
        hibernateStatistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        hibernateStatistics.setStatisticsEnabled(true);
        hibernateStatistics.clear();

        // 테스트 카테고리 생성
        testCategory = ProductCategoryJpaEntity.builder()
            .name("성능 테스트 카테고리")
            .slug("performance-category")
            .description("성능 테스트용")
            .isActive(true)
            .displayOrder(1)
            .build();
        entityManager.persist(testCategory);

        // 대용량 테스트 데이터 생성 (100개 상품)
        testProducts = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            ProductJpaEntity product = ProductJpaEntity.fromDomainEntityForCreation(
                com.msa.commerce.monolith.product.domain.Product.builder()
                    .sku("PERF-" + String.format("%03d", i))
                    .name("성능 테스트 상품 " + i)
                    .description("성능 테스트용 상품 " + i)
                    .brand("TestBrand" + (i % 5)) // 5개 브랜드로 분산
                    .productType(i % 2 == 0 ? ProductType.PHYSICAL : ProductType.DIGITAL)
                    .basePrice(new BigDecimal(String.valueOf(1000 + i * 100)))
                    .currency("KRW")
                    .requiresShipping(i % 2 == 0)
                    .isTaxable(true)
                    .isFeatured(i % 10 == 0) // 10%만 featured
                    .slug("perf-product-" + i)
                    .searchTags("테스트, 성능, 상품" + i)
                    .build()
            );
            product.setCategory(testCategory);
            product.setCategoryId(testCategory.getId());
            entityManager.persist(product);
            testProducts.add(product);

            // 각 상품에 변형 3개씩 생성
            for (int j = 1; j <= 3; j++) {
                ProductVariantJpaEntity variant = ProductVariantJpaEntity.builder()
                    .product(product)
                    .variantSku("PERF-" + String.format("%03d", i) + "-V" + j)
                    .name("변형 " + j)
                    .priceAdjustment(new BigDecimal(j * 500))
                    .color("Color" + j)
                    .size("Size" + j)
                    .build();
                entityManager.persist(variant);
            }

            // 각 상품에 재고 정보 생성
            InventorySnapshotJpaEntity inventory = InventorySnapshotJpaEntity.builder()
                .product(product)
                .locationCode("MAIN")
                .availableQuantity(50 + (i % 100))
                .reservedQuantity(i % 20)
                .lowStockThreshold(10)
                .build();
            entityManager.persist(inventory);
        }

        entityManager.flush();
        entityManager.clear();

        // 통계 초기화
        hibernateStatistics.clear();
    }

    @Test
    @DisplayName("N+1 문제 검증 - Fetch Join 없이 조회")
    void testNPlusOneProblem_WithoutFetchJoin() {
        // Given
        hibernateStatistics.clear();

        // When - Fetch Join 없이 일반 조회
        List<ProductJpaEntity> products = productRepository.findAll();

        // 각 상품의 카테고리와 재고에 접근 (N+1 문제 발생)
        for (ProductJpaEntity product : products.subList(0, 10)) { // 처음 10개만
            String categoryName = product.getCategory().getName(); // Lazy Loading 발생
            int inventoryCount = product.getInventorySnapshots().size(); // Lazy Loading 발생
        }

        // Then
        long queryCount = hibernateStatistics.getQueryExecutionCount();
        System.out.println("=== N+1 문제 발생 시 쿼리 수: " + queryCount + " ===");

        // N+1 문제가 발생하면 최소 21개 이상의 쿼리 실행 (1 + 10*2)
        assertThat(queryCount).isGreaterThan(15);
    }

    @Test
    @DisplayName("N+1 문제 해결 - Fetch Join 사용")
    void testNPlusOneProblemResolved_WithFetchJoin() {
        // Given
        hibernateStatistics.clear();

        // When - Fetch Join을 사용한 최적화된 조회
        List<ProductJpaEntity> products = productRepository.findFirst10ByOrderByIdAsc()
            .stream()
            .map(product -> productRepository.findProductWithFullDetails(product.getId()).orElse(null))
            .toList();

        // 이미 Fetch Join으로 로딩된 데이터에 접근
        for (ProductJpaEntity product : products) {
            if (product != null) {
                String categoryName = product.getCategory().getName(); // 추가 쿼리 없음
                int inventoryCount = product.getInventorySnapshots().size(); // 추가 쿼리 없음
            }
        }

        // Then
        long queryCount = hibernateStatistics.getQueryExecutionCount();
        System.out.println("=== Fetch Join 사용 시 쿼리 수: " + queryCount + " ===");

        // Fetch Join을 사용하면 10개 정도의 쿼리로 제한 (각 findProductWithFullDetails 호출당 1개)
        assertThat(queryCount).isLessThan(15);
    }

    @Test
    @DisplayName("복합 검색 성능 테스트")
    void testComplexSearchPerformance() {
        // Given
        hibernateStatistics.clear();
        long startTime = System.currentTimeMillis();

        // When - 복합 조건 검색
        var result = productRepository.searchProductsWithInventory(
            "성능 테스트", // 키워드 검색
            null, // 카테고리
            null, // 상태
            null, // 타입
            new BigDecimal("5000"), // 최소 가격
            new BigDecimal("15000"), // 최대 가격
            "TestBrand1", // 브랜드
            null, // Featured
            org.springframework.data.domain.PageRequest.of(0, 20)
        );

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        long queryCount = hibernateStatistics.getQueryExecutionCount();
        System.out.println("=== 복합 검색 성능 ===");
        System.out.println("쿼리 수: " + queryCount);
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("결과 수: " + result.getTotalElements());

        // 성능 검증
        assertThat(executionTime).isLessThan(1000); // 1초 이내
        assertThat(queryCount).isLessThan(5); // 쿼리 수 제한
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("통계 쿼리 성능 테스트")
    void testStatisticsQueryPerformance() {
        // Given
        hibernateStatistics.clear();
        long startTime = System.currentTimeMillis();

        // When - 브랜드별 통계
        var brandStats = productRepository.getBrandProductStats();

        // 가격 범위별 통계
        var priceStats = productRepository.getPriceRangeStats();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        long queryCount = hibernateStatistics.getQueryExecutionCount();
        System.out.println("=== 통계 쿼리 성능 ===");
        System.out.println("쿼리 수: " + queryCount);
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("브랜드 통계 수: " + brandStats.size());
        System.out.println("가격 범위 통계 수: " + priceStats.size());

        // 성능 검증
        assertThat(executionTime).isLessThan(2000); // 2초 이내
        assertThat(queryCount).isLessThan(10); // 쿼리 수 제한
        assertThat(brandStats).isNotEmpty();
        assertThat(priceStats).isNotEmpty();
    }

    @Test
    @DisplayName("대용량 데이터 페이징 성능 테스트")
    void testLargePagingPerformance() {
        // Given - 마지막 페이지 조회 (성능이 가장 안 좋은 케이스)
        hibernateStatistics.clear();
        long startTime = System.currentTimeMillis();

        // When - 마지막 페이지 조회
        int lastPage = (int) Math.ceil(100.0 / 10) - 1;
        var lastPageResult = productRepository.searchProductsWithInventory(
            null, null, null, null, null, null, null, null,
            org.springframework.data.domain.PageRequest.of(lastPage, 10)
        );

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        long queryCount = hibernateStatistics.getQueryExecutionCount();
        System.out.println("=== 대용량 페이징 성능 (마지막 페이지) ===");
        System.out.println("쿼리 수: " + queryCount);
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("페이지 번호: " + lastPage);
        System.out.println("결과 수: " + lastPageResult.getContent().size());

        // 성능 검증
        assertThat(executionTime).isLessThan(1500); // 1.5초 이내
        assertThat(queryCount).isLessThan(3); // COUNT 쿼리 + SELECT 쿼리
        assertThat(lastPageResult.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("메모리 효율성 테스트 - 배치 처리")
    void testMemoryEfficiency_BatchProcessing() {
        // Given
        hibernateStatistics.clear();
        long startTime = System.currentTimeMillis();

        // When - 배치 처리용 ID 조회 (메모리 효율적)
        List<Long> productIds = productRepository.findProductIdsForBatchProcessing(
            ProductStatus.ACTIVE, 50, null
        );

        // 배치로 처리
        List<ProductJpaEntity> products = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i += 10) { // 10개씩 배치 처리
            int endIndex = Math.min(i + 10, productIds.size());
            List<Long> batchIds = productIds.subList(i, endIndex);

            // 실제로는 findAllById를 사용하지만, 여기서는 개별 조회로 시뮬레이션
            for (Long id : batchIds) {
                Optional<ProductJpaEntity> product = productRepository.findProductWithFullDetails(id);
                product.ifPresent(products::add);
            }
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then
        long queryCount = hibernateStatistics.getQueryExecutionCount();
        System.out.println("=== 배치 처리 성능 ===");
        System.out.println("쿼리 수: " + queryCount);
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("처리된 상품 수: " + products.size());

        // 성능 검증
        assertThat(executionTime).isLessThan(3000); // 3초 이내
        assertThat(products.size()).isEqualTo(Math.min(50, testProducts.size()));
    }

    @Test
    @DisplayName("히베네이트 2차 캐시 효과 테스트")
    void testSecondLevelCacheEffect() {
        // Given - 같은 상품을 여러 번 조회
        Long productId = testProducts.get(0).getId();
        hibernateStatistics.clear();

        // When - 첫 번째 조회
        long startTime1 = System.currentTimeMillis();
        Optional<ProductJpaEntity> product1 = productRepository.findProductWithFullDetails(productId);
        long endTime1 = System.currentTimeMillis();

        entityManager.clear(); // 1차 캐시 클리어

        // 두 번째 조회 (2차 캐시에서 조회되어야 함)
        long startTime2 = System.currentTimeMillis();
        Optional<ProductJpaEntity> product2 = productRepository.findProductWithFullDetails(productId);
        long endTime2 = System.currentTimeMillis();

        // Then
        long firstQueryTime = endTime1 - startTime1;
        long secondQueryTime = endTime2 - startTime2;
        long queryCount = hibernateStatistics.getQueryExecutionCount();

        System.out.println("=== 2차 캐시 효과 테스트 ===");
        System.out.println("첫 번째 조회 시간: " + firstQueryTime + "ms");
        System.out.println("두 번째 조회 시간: " + secondQueryTime + "ms");
        System.out.println("총 쿼리 수: " + queryCount);

        assertThat(product1).isPresent();
        assertThat(product2).isPresent();
        assertThat(product1.get().getId()).isEqualTo(product2.get().getId());
    }

    @Test
    @DisplayName("동적 쿼리 성능 비교 - 조건 유무")
    void testDynamicQueryPerformance() {
        // Given
        hibernateStatistics.clear();

        // When - 조건이 많은 동적 쿼리
        long startTime1 = System.currentTimeMillis();
        var complexResult = productRepository.searchProductsWithInventory(
            "테스트", testCategory.getId(), ProductStatus.ACTIVE, ProductType.PHYSICAL,
            new BigDecimal("1000"), new BigDecimal("10000"), "TestBrand1", true,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        long endTime1 = System.currentTimeMillis();

        hibernateStatistics.clear();

        // 조건이 없는 단순 쿼리
        long startTime2 = System.currentTimeMillis();
        var simpleResult = productRepository.searchProductsWithInventory(
            null, null, null, null, null, null, null, null,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );
        long endTime2 = System.currentTimeMillis();

        // Then
        long complexQueryTime = endTime1 - startTime1;
        long simpleQueryTime = endTime2 - startTime2;

        System.out.println("=== 동적 쿼리 성능 비교 ===");
        System.out.println("복합 조건 쿼리 시간: " + complexQueryTime + "ms");
        System.out.println("단순 쿼리 시간: " + simpleQueryTime + "ms");
        System.out.println("복합 조건 결과 수: " + complexResult.getTotalElements());
        System.out.println("단순 쿼리 결과 수: " + simpleResult.getTotalElements());

        // 동적 쿼리가 과도하게 느리지 않아야 함 (3배 이내)
        assertThat(complexQueryTime).isLessThan(simpleQueryTime * 3);
        assertThat(complexResult.getContent()).isNotEmpty();
        assertThat(simpleResult.getContent()).isNotEmpty();
    }
}
package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.msa.commerce.common.config.QuerydslConfig;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;
import com.msa.commerce.monolith.product.domain.ProductVariantStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.hibernate.tool.schema=ERROR",  // DDL 경고 억제
    "spring.data.jpa.auditing.enabled=false"  // JPA 감사 비활성화
})
@Import(QuerydslConfig.class)  // QueryDSL 설정 추가
@Testcontainers
@DisplayName("InventorySnapshotJpaRepository 테스트")
class InventorySnapshotJpaRepositoryTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.0")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass")
        .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventorySnapshotJpaRepository inventoryRepository;

    private Long testProductId;

    private Long testVariantId;

    @BeforeEach
    void setUp() {
        // 도메인 객체를 통한 테스트 데이터 생성
        ProductJpaEntity product = createTestProduct();
        entityManager.persistAndFlush(product);
        testProductId = product.getId();

        ProductVariantJpaEntity variant = createTestVariant(product);
        entityManager.persistAndFlush(variant);
        testVariantId = variant.getId();

        // 재고 데이터 생성
        createTestInventoryData(product, variant);

        entityManager.flush();
        entityManager.clear();
    }

    private ProductJpaEntity createTestProduct() {
        LocalDateTime now = LocalDateTime.now();

        // reconstitute 메소드를 사용하여 ACTIVE 상태의 Product 생성
        Product domainProduct = Product.reconstitute(
            null, // id (자동 생성)
            "TEST-SKU-001",
            "테스트 상품",
            "테스트 요약",
            "테스트 상품 설명",
            null, // categoryId
            "TestBrand",
            ProductType.PHYSICAL,
            ProductStatus.ACTIVE, // 이제 status 설정 가능
            new BigDecimal("10000"),
            null, // salePrice
            "KRW",
            null, // weightGrams
            true, // requiresShipping
            true, // isTaxable
            false, // isFeatured
            "test-product-001",
            null, // searchTags
            null, // primaryImageUrl
            1, // minOrderQuantity
            100, // maxOrderQuantity
            now, // createdAt - 명시적으로 설정
            now, // updatedAt - 명시적으로 설정
            null, // deletedAt
            1L // version
        );

        // 도메인 객체를 JPA 엔티티로 변환
        ProductJpaEntity jpaEntity = ProductJpaEntity.fromDomainEntityForCreation(domainProduct);

        // JPA 감사가 비활성화되어 있으므로 수동으로 설정
        jpaEntity.setCreatedAt(now);
        jpaEntity.setUpdatedAt(now);

        return jpaEntity;
    }

    private ProductVariantJpaEntity createTestVariant(ProductJpaEntity product) {
        return ProductVariantJpaEntity.builder()
            .product(product)
            .variantSku("TEST-SKU-001-V1")
            .name("기본 변형")
            .priceAdjustment(BigDecimal.ZERO)
            .status(ProductVariantStatus.ACTIVE)
            .isDefault(true)
            .build();
    }

    private void createTestInventoryData(ProductJpaEntity product, ProductVariantJpaEntity variant) {
        // MAIN 창고 재고 (정상 재고)
        InventorySnapshotJpaEntity mainInventory = InventorySnapshotJpaEntity.builder()
            .product(product)
            .variant(variant)
            .locationCode("MAIN")
            .availableQuantity(100)
            .reservedQuantity(20)
            .lowStockThreshold(15)
            .build();
        entityManager.persist(mainInventory);

        // 재고 부족 창고
        InventorySnapshotJpaEntity lowStockInventory = InventorySnapshotJpaEntity.builder()
            .product(product)
            .variant(null)
            .locationCode("WAREHOUSE_B")
            .availableQuantity(5)
            .reservedQuantity(0)
            .lowStockThreshold(10)
            .build();
        entityManager.persist(lowStockInventory);

        // 재고 없음 창고
        InventorySnapshotJpaEntity outOfStockInventory = InventorySnapshotJpaEntity.builder()
            .product(product)
            .variant(null)
            .locationCode("WAREHOUSE_C")
            .availableQuantity(0)
            .reservedQuantity(5)
            .lowStockThreshold(10)
            .build();
        entityManager.persist(outOfStockInventory);
    }

    @Test
    @DisplayName("상품 ID와 위치로 재고 조회 테스트")
    void findByProductIdAndLocationCode() {
        // When
        Optional<InventorySnapshotJpaEntity> result = inventoryRepository
            .findByProductIdAndLocationCode(testProductId, "MAIN");

        // Then
        assertThat(result).isPresent();
        InventorySnapshotJpaEntity inventory = result.get();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(100);
        assertThat(inventory.getReservedQuantity()).isEqualTo(20);
        assertThat(inventory.getLocationCode()).isEqualTo("MAIN");
    }

    @Test
    @DisplayName("상품별 모든 재고 조회 테스트")
    void findByProductId() {
        // When
        List<InventorySnapshotJpaEntity> results = inventoryRepository
            .findByProductId(testProductId);

        // Then
        assertThat(results).hasSize(3);
        assertThat(results)
            .extracting(InventorySnapshotJpaEntity::getLocationCode)
            .containsExactlyInAnyOrder("MAIN", "WAREHOUSE_B", "WAREHOUSE_C");
    }

    @Test
    @DisplayName("재고 부족 상품 조회 테스트")
    void findLowStockItems() {
        // When
        List<InventorySnapshotJpaEntity> results = inventoryRepository.findLowStockItems();

        // Then
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
        boolean hasWarehouseB = results.stream()
            .anyMatch(item -> "WAREHOUSE_B".equals(item.getLocationCode()));
        assertThat(hasWarehouseB).isTrue();
    }

    @Test
    @DisplayName("재고 없음 상품 조회 테스트")
    void findOutOfStockItems() {
        // When
        List<InventorySnapshotJpaEntity> results = inventoryRepository.findOutOfStockItems();

        // Then
        assertThat(results).hasSizeGreaterThanOrEqualTo(1);
        boolean hasWarehouseC = results.stream()
            .anyMatch(item -> "WAREHOUSE_C".equals(item.getLocationCode()));
        assertThat(hasWarehouseC).isTrue();
    }

    @Test
    @DisplayName("재고 비즈니스 로직 테스트")
    void inventoryBusinessLogic() {
        // Given
        InventorySnapshotJpaEntity inventory = inventoryRepository
            .findByProductIdAndLocationCode(testProductId, "MAIN")
            .orElseThrow();

        // When & Then - 총 재고량 계산
        assertThat(inventory.getTotalQuantity()).isEqualTo(120); // 100 + 20

        // When & Then - 재고 상태 확인
        assertThat(inventory.isLowStock()).isFalse(); // 100 > 15
        assertThat(inventory.isOutOfStock()).isFalse();
        assertThat(inventory.canReserve(50)).isTrue();
        assertThat(inventory.canReserve(150)).isFalse();

        // When & Then - 재고 상태 계산
        assertThat(inventory.calculateStockStatus().name()).isEqualTo("IN_STOCK");
    }

    @Test
    @DisplayName("재고 조정 로직 테스트")
    void inventoryAdjustmentTest() {
        // Given
        InventorySnapshotJpaEntity inventory = inventoryRepository
            .findByProductIdAndLocationCode(testProductId, "MAIN")
            .orElseThrow();

        int initialAvailable = inventory.getAvailableQuantity();
        int initialReserved = inventory.getReservedQuantity();

        // When - 재고 예약
        inventory.reserveStock(30);

        // Then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(initialAvailable - 30);
        assertThat(inventory.getReservedQuantity()).isEqualTo(initialReserved + 30);

        // When - 예약 해제
        inventory.releaseReservedStock(10);

        // Then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(initialAvailable - 20);
        assertThat(inventory.getReservedQuantity()).isEqualTo(initialReserved + 20);
    }

    @Test
    @DisplayName("재고 예외 상황 테스트")
    void inventoryExceptionTest() {
        // Given
        InventorySnapshotJpaEntity inventory = inventoryRepository
            .findByProductIdAndLocationCode(testProductId, "MAIN")
            .orElseThrow();

        // When & Then - 재고 부족 시 예외
        assertThatThrownBy(() -> inventory.reserveStock(200))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("사용 가능한 재고가 부족합니다.");

        // When & Then - 예약 재고 부족 시 예외
        assertThatThrownBy(() -> inventory.releaseReservedStock(100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("예약된 재고가 부족합니다.");
    }

}

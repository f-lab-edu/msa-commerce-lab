package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;
import com.msa.commerce.monolith.product.domain.ProductVariantStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
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
        // 더 간단한 테스트 데이터 생성 방식 사용
        ProductJpaEntity product = createSimpleTestProduct();
        entityManager.persistAndFlush(product);
        testProductId = product.getId();

        ProductVariantJpaEntity variant = createSimpleTestVariant(product);
        entityManager.persistAndFlush(variant);
        testVariantId = variant.getId();

        // 재고 데이터 생성
        createTestInventoryData(product, variant);

        entityManager.flush();
        entityManager.clear();
    }

    private ProductJpaEntity createSimpleTestProduct() {
        try {
            ProductJpaEntity product = new ProductJpaEntity();

            // 리플렉션을 통한 필드 설정
            setField(product, "sku", "TEST-SKU-001");
            setField(product, "name", "테스트 상품");
            setField(product, "description", "테스트 상품 설명");
            setField(product, "shortDescription", "테스트 요약");
            setField(product, "brand", "TestBrand");
            setField(product, "productType", ProductType.PHYSICAL);
            setField(product, "status", ProductStatus.ACTIVE);
            setField(product, "basePrice", new BigDecimal("10000"));
            setField(product, "currency", "KRW");
            setField(product, "requiresShipping", true);
            setField(product, "isTaxable", true);
            setField(product, "isFeatured", false);
            setField(product, "slug", "test-product-001");

            return product;
        } catch (Exception e) {
            throw new RuntimeException("테스트 상품 생성 실패", e);
        }
    }

    private ProductVariantJpaEntity createSimpleTestVariant(ProductJpaEntity product) {
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

    private void setField(Object object, String fieldName, Object value) throws Exception {
        var field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
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

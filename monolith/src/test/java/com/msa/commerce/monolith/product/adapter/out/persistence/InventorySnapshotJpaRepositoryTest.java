package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

@DataJpaTest
@DisplayName("InventorySnapshotJpaRepository 테스트")
@org.junit.jupiter.api.Disabled("Inventory 엔티티 테이블 생성 문제로 인해 임시 비활성화 - H2 JSON 컬럼 호환성 문제")
class InventorySnapshotJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventorySnapshotJpaRepository inventoryRepository;

    private ProductJpaEntity testProduct;
    private ProductVariantJpaEntity testVariant;
    private InventorySnapshotJpaEntity testInventory;

    @BeforeEach
    void setUp() {
        // 테스트 상품 생성
        testProduct = ProductJpaEntity.fromDomainEntityForCreation(
            com.msa.commerce.monolith.product.domain.Product.builder()
                .sku("INV-TEST-001")
                .name("재고 테스트 상품")
                .description("재고 관리 테스트용 상품")
                .brand("TestBrand")
                .productType(ProductType.PHYSICAL)
                .basePrice(new BigDecimal("15000"))
                .currency("KRW")
                .slug("inventory-test-product")
                .build()
        );
        entityManager.persist(testProduct);

        // 테스트 상품 변형 생성
        testVariant = ProductVariantJpaEntity.builder()
            .product(testProduct)
            .variantSku("INV-TEST-001-RED")
            .name("빨간색 변형")
            .priceAdjustment(new BigDecimal("1000"))
            .color("red")
            .size("M")
            .build();
        entityManager.persist(testVariant);

        // 테스트 재고 스냅샷 생성
        testInventory = InventorySnapshotJpaEntity.builder()
            .product(testProduct)
            .variant(testVariant)
            .locationCode("MAIN")
            .availableQuantity(100)
            .reservedQuantity(20)
            .lowStockThreshold(15)
            .build();
        entityManager.persist(testInventory);

        // 재고 부족 테스트를 위한 추가 재고
        InventorySnapshotJpaEntity lowStockInventory = InventorySnapshotJpaEntity.builder()
            .product(testProduct)
            .locationCode("WAREHOUSE_B")
            .availableQuantity(5)
            .reservedQuantity(0)
            .lowStockThreshold(10)
            .build();
        entityManager.persist(lowStockInventory);

        // 재고 없음 테스트를 위한 재고
        InventorySnapshotJpaEntity outOfStockInventory = InventorySnapshotJpaEntity.builder()
            .product(testProduct)
            .locationCode("WAREHOUSE_C")
            .availableQuantity(0)
            .reservedQuantity(5)
            .lowStockThreshold(10)
            .build();
        entityManager.persist(outOfStockInventory);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("상품 ID와 위치로 재고 조회")
    void findByProductIdAndLocationCode() {
        // When
        Optional<InventorySnapshotJpaEntity> result = inventoryRepository
            .findByProductIdAndLocationCode(testProduct.getId(), "MAIN");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAvailableQuantity()).isEqualTo(100);
        assertThat(result.get().getReservedQuantity()).isEqualTo(20);
    }

    @Test
    @DisplayName("상품, 변형, 위치로 재고 조회")
    void findByProductIdAndVariantIdAndLocationCode() {
        // When
        Optional<InventorySnapshotJpaEntity> result = inventoryRepository
            .findByProductIdAndVariantIdAndLocationCode(
                testProduct.getId(), testVariant.getId(), "MAIN");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getVariant().getId()).isEqualTo(testVariant.getId());
    }

    @Test
    @DisplayName("상품별 모든 재고 조회")
    void findByProductId() {
        // When
        List<InventorySnapshotJpaEntity> result = inventoryRepository
            .findByProductId(testProduct.getId());

        // Then
        assertThat(result).hasSize(3); // MAIN, WAREHOUSE_B, WAREHOUSE_C
        assertThat(result)
            .extracting(InventorySnapshotJpaEntity::getLocationCode)
            .contains("MAIN", "WAREHOUSE_B", "WAREHOUSE_C");
    }

    @Test
    @DisplayName("재고 부족 상품 조회")
    void findLowStockItems() {
        // When
        List<InventorySnapshotJpaEntity> result = inventoryRepository.findLowStockItems();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationCode()).isEqualTo("WAREHOUSE_B");
        assertThat(result.get(0).getAvailableQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("재고 없음 상품 조회")
    void findOutOfStockItems() {
        // When
        List<InventorySnapshotJpaEntity> result = inventoryRepository.findOutOfStockItems();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationCode()).isEqualTo("WAREHOUSE_C");
        assertThat(result.get(0).getAvailableQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품별 총 사용 가능 재고 조회")
    void getTotalAvailableQuantityByProductId() {
        // When
        Integer totalAvailable = inventoryRepository
            .getTotalAvailableQuantityByProductId(testProduct.getId());

        // Then
        assertThat(totalAvailable).isEqualTo(105); // 100 + 5 + 0
    }

    @Test
    @DisplayName("상품별 총 예약 재고 조회")
    void getTotalReservedQuantityByProductId() {
        // When
        Integer totalReserved = inventoryRepository
            .getTotalReservedQuantityByProductId(testProduct.getId());

        // Then
        assertThat(totalReserved).isEqualTo(25); // 20 + 0 + 5
    }

    @Test
    @DisplayName("재고 부족 상품 개수 조회")
    void countLowStockItems() {
        // When
        long count = inventoryRepository.countLowStockItems();

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("재고 없음 상품 개수 조회")
    void countOutOfStockItems() {
        // When
        long count = inventoryRepository.countOutOfStockItems();

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("재고 예약 가능 여부 확인")
    void canReserveStock() {
        // When - 예약 가능한 경우
        boolean canReserve1 = inventoryRepository.canReserveStock(
            testProduct.getId(), testVariant.getId(), "MAIN", 50);

        // When - 예약 불가능한 경우
        boolean canReserve2 = inventoryRepository.canReserveStock(
            testProduct.getId(), testVariant.getId(), "MAIN", 150);

        // Then
        assertThat(canReserve1).isTrue();
        assertThat(canReserve2).isFalse();
    }

    @Test
    @DisplayName("위치별 재고 조회 (최근 업데이트 순)")
    void findByLocationCodeOrderByLastUpdatedAtDesc() {
        // When
        List<InventorySnapshotJpaEntity> result = inventoryRepository
            .findByLocationCodeOrderByLastUpdatedAtDesc("MAIN");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationCode()).isEqualTo("MAIN");
    }

    @Test
    @DisplayName("재고 스냅샷 비즈니스 로직 테스트")
    void inventorySnapshotBusinessLogic() {
        // When
        InventorySnapshotJpaEntity inventory = inventoryRepository
            .findByProductIdAndLocationCode(testProduct.getId(), "MAIN")
            .orElseThrow();

        // Then
        assertThat(inventory.getTotalQuantity()).isEqualTo(120); // 100 + 20
        assertThat(inventory.isLowStock()).isFalse(); // 100 > 15
        assertThat(inventory.isOutOfStock()).isFalse();
        assertThat(inventory.canReserve(50)).isTrue();
        assertThat(inventory.canReserve(150)).isFalse();
        assertThat(inventory.calculateStockStatus().name()).isEqualTo("IN_STOCK");
    }

    @Test
    @DisplayName("재고 조정 비즈니스 로직 테스트")
    void inventoryAdjustmentLogic() {
        // Given
        InventorySnapshotJpaEntity inventory = inventoryRepository
            .findByProductIdAndLocationCode(testProduct.getId(), "MAIN")
            .orElseThrow();

        // When - 재고 증가
        inventory.adjustAvailableQuantity(50);

        // Then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(150);

        // When - 재고 예약
        inventory.reserveStock(30);

        // Then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(120); // 150 - 30
        assertThat(inventory.getReservedQuantity()).isEqualTo(50); // 20 + 30

        // When - 예약 해제
        inventory.releaseReservedStock(20);

        // Then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(140); // 120 + 20
        assertThat(inventory.getReservedQuantity()).isEqualTo(30); // 50 - 20

        // When - 예약 확정
        inventory.confirmReservedStock(30);

        // Then
        assertThat(inventory.getReservedQuantity()).isEqualTo(0); // 30 - 30
    }

    @Test
    @DisplayName("재고 조정 예외 상황 테스트")
    void inventoryAdjustmentExceptions() {
        // Given
        InventorySnapshotJpaEntity inventory = inventoryRepository
            .findByProductIdAndLocationCode(testProduct.getId(), "MAIN")
            .orElseThrow();

        // When & Then - 사용 가능한 재고가 부족한 경우
        assertThatThrownBy(() -> inventory.reserveStock(150))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("사용 가능한 재고가 부족합니다.");

        // When & Then - 예약된 재고보다 많이 해제하려는 경우
        assertThatThrownBy(() -> inventory.releaseReservedStock(50))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("예약된 재고가 부족합니다.");

        // When & Then - 음수 재고로 조정하려는 경우
        assertThatThrownBy(() -> inventory.adjustAvailableQuantity(-200))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("사용 가능한 재고가 0보다 작을 수 없습니다.");
    }
}
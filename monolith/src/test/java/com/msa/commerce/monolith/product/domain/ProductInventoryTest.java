package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductInventoryTest {

    @Test
    @DisplayName("재고를 정상적으로 생성할 수 있다")
    void createInventory_ShouldCreateSuccessfully() {
        // given & when
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(90)
            .reservedQuantity(10)
            .totalQuantity(100)
            .lowStockThreshold(5)
            .isTrackingEnabled(true)
            .isBackorderAllowed(false)
            .minOrderQuantity(1)
            .maxOrderQuantity(50)
            .reorderPoint(20)
            .reorderQuantity(100)
            .locationCode("MAIN")
            .build();

        // then
        assertThat(inventory.getProductId()).isEqualTo(1L);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(90);
        assertThat(inventory.getReservedQuantity()).isEqualTo(10);
        assertThat(inventory.getTotalQuantity()).isEqualTo(100);
        assertThat(inventory.getMinOrderQuantity()).isEqualTo(1);
        assertThat(inventory.getMaxOrderQuantity()).isEqualTo(50);
        assertThat(inventory.getReorderPoint()).isEqualTo(20);
        assertThat(inventory.getReorderQuantity()).isEqualTo(100);
        assertThat(inventory.getLocationCode()).isEqualTo("MAIN");
        assertThat(inventory.getVersionNumber()).isEqualTo(0L);
    }

    @Test
    @DisplayName("필수 필드가 null일 때 예외를 발생시킨다")
    void createInventory_WithNullProductId_ShouldThrowException() {
        // when & then
        assertThatThrownBy(() ->
            ProductInventory.builder()
                .productId(null)
                .availableQuantity(100)
                .totalQuantity(100)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product ID is required");
    }

    @Test
    @DisplayName("재고 수량이 음수일 때 예외를 발생시킨다")
    void createInventory_WithNegativeQuantity_ShouldThrowException() {
        // when & then
        assertThatThrownBy(() ->
            ProductInventory.builder()
                .productId(1L)
                .availableQuantity(-1)
                .totalQuantity(100)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Inventory quantities cannot be negative");
    }

    @Test
    @DisplayName("재고를 성공적으로 차감할 수 있다")
    void decreaseStock_ShouldDecreaseSuccessfully() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .build();

        // when
        inventory.decreaseStock(10);

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(90);
    }

    @Test
    @DisplayName("재고가 부족할 때 차감 시 예외를 발생시킨다")
    void decreaseStock_InsufficientStock_ShouldThrowException() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(5)
            .totalQuantity(5)
            .build();

        // when & then
        assertThatThrownBy(() -> inventory.decreaseStock(10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Insufficient available stock");
    }

    @Test
    @DisplayName("재고를 성공적으로 증가시킬 수 있다")
    void increaseStock_ShouldIncreaseSuccessfully() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .build();

        // when
        inventory.increaseStock(20);

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(120);
        assertThat(inventory.getTotalQuantity()).isEqualTo(120);
    }

    @Test
    @DisplayName("재고를 성공적으로 예약할 수 있다")
    void reserveStock_ShouldReserveSuccessfully() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .reservedQuantity(0)
            .totalQuantity(100)
            .build();

        // when
        inventory.reserveStock(10);

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(90);
        assertThat(inventory.getReservedQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("예약된 재고를 성공적으로 해제할 수 있다")
    void releaseReserved_ShouldReleaseSuccessfully() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(90)
            .reservedQuantity(10)
            .totalQuantity(100)
            .build();

        // when
        inventory.releaseReserved(5);

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(95);
        assertThat(inventory.getReservedQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("재고 부족 상태를 정확히 판단한다")
    void isLowStock_ShouldReturnCorrectStatus() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(5)
            .totalQuantity(5)
            .lowStockThreshold(10)
            .isTrackingEnabled(true)
            .build();

        // when & then
        assertThat(inventory.isLowStock()).isTrue();
    }

    @Test
    @DisplayName("품절 상태를 정확히 판단한다")
    void isOutOfStock_ShouldReturnCorrectStatus() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(0)
            .totalQuantity(0)
            .build();

        // when & then
        assertThat(inventory.isOutOfStock()).isTrue();
    }

    @Test
    @DisplayName("재주문이 필요한 상태를 정확히 판단한다")
    void isReorderNeeded_ShouldReturnCorrectStatus() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(15)
            .totalQuantity(15)
            .reorderPoint(20)
            .isTrackingEnabled(true)
            .build();

        // when & then
        assertThat(inventory.isReorderNeeded()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("주문 가능 수량 검증을 정확히 수행한다")
    @ValueSource(ints = {0, 51})
    void canOrder_WithInvalidQuantity_ShouldReturnFalse(int quantity) {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .minOrderQuantity(1)
            .maxOrderQuantity(50)
            .isTrackingEnabled(true)
            .build();

        // when & then
        assertThat(inventory.canOrder(quantity)).isFalse();
    }

    @Test
    @DisplayName("유효한 수량으로 주문 가능하다")
    void canOrder_WithValidQuantity_ShouldReturnTrue() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .minOrderQuantity(1)
            .maxOrderQuantity(50)
            .isTrackingEnabled(true)
            .build();

        // when & then
        assertThat(inventory.canOrder(25)).isTrue();
    }

    @Test
    @DisplayName("백오더 허용 시 재고 부족해도 주문 가능하다")
    void canOrder_WithBackorderAllowed_ShouldReturnTrue() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(5)
            .totalQuantity(5)
            .minOrderQuantity(1)
            .maxOrderQuantity(50)
            .isTrackingEnabled(true)
            .isBackorderAllowed(true)
            .build();

        // when & then
        assertThat(inventory.canOrder(10)).isTrue();
    }

    @Test
    @DisplayName("주문 이행 가능성을 정확히 판단한다")
    void canFulfillOrder_ShouldReturnCorrectResult() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .minOrderQuantity(1)
            .maxOrderQuantity(50)
            .isTrackingEnabled(true)
            .isBackorderAllowed(false)
            .build();

        // when & then
        assertThat(inventory.canFulfillOrder(25)).isTrue();
        assertThat(inventory.canFulfillOrder(150)).isFalse();
    }

    @Test
    @DisplayName("재고 조정을 성공적으로 수행한다")
    void adjustStock_ShouldAdjustSuccessfully() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .build();

        // when - positive adjustment
        inventory.adjustStock(20, "Incoming shipment");

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(120);
        assertThat(inventory.getTotalQuantity()).isEqualTo(120);

        // when - negative adjustment
        inventory.adjustStock(-10, "Damaged goods");

        // then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(110);
    }

    @Test
    @DisplayName("예약 가능한 재고량을 정확히 반환한다")
    void getAvailableToReserve_ShouldReturnCorrectAmount() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .build();

        // when & then
        assertThat(inventory.getAvailableToReserve()).isEqualTo(100);
    }

    @Test
    @DisplayName("미처리 수량을 정확히 계산한다")
    void getUncommittedQuantity_ShouldReturnCorrectAmount() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(80)
            .reservedQuantity(20)
            .totalQuantity(110)
            .build();

        // when & then
        assertThat(inventory.getUncommittedQuantity()).isEqualTo(10); // 110 - 80 - 20 = 10
    }

    @Test
    @DisplayName("버전 번호를 성공적으로 업데이트한다")
    void updateVersionNumber_ShouldIncreaseVersion() {
        // given
        ProductInventory inventory = ProductInventory.builder()
            .productId(1L)
            .availableQuantity(100)
            .totalQuantity(100)
            .build();

        long initialVersion = inventory.getVersionNumber();

        // when
        inventory.updateVersionNumber();

        // then
        assertThat(inventory.getVersionNumber()).isEqualTo(initialVersion + 1);
    }

    @Test
    @DisplayName("최소/최대 주문 수량 유효성 검증을 수행한다")
    void createInventory_WithInvalidOrderQuantities_ShouldThrowException() {
        // when & then - min order quantity is 0 or negative
        assertThatThrownBy(() ->
            ProductInventory.builder()
                .productId(1L)
                .minOrderQuantity(0)
                .totalQuantity(100)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Minimum order quantity must be positive");

        // when & then - max order quantity is less than min
        assertThatThrownBy(() ->
            ProductInventory.builder()
                .productId(1L)
                .minOrderQuantity(10)
                .maxOrderQuantity(5)
                .totalQuantity(100)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Maximum order quantity cannot be less than minimum order quantity");
    }

    @Test
    @DisplayName("재주문 관련 필드 유효성 검증을 수행한다")
    void createInventory_WithInvalidReorderFields_ShouldThrowException() {
        // when & then - negative reorder point
        assertThatThrownBy(() ->
            ProductInventory.builder()
                .productId(1L)
                .reorderPoint(-1)
                .totalQuantity(100)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Reorder point cannot be negative");

        // when & then - negative reorder quantity
        assertThatThrownBy(() ->
            ProductInventory.builder()
                .productId(1L)
                .reorderQuantity(-1)
                .totalQuantity(100)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Reorder quantity cannot be negative");
    }

    @Test
    @DisplayName("위치 코드 유효성 검증을 수행한다")
    void createInventory_WithInvalidLocationCode_ShouldThrowException() {
        // when & then
        assertThatThrownBy(() ->
            ProductInventory.builder()
                .productId(1L)
                .locationCode("")
                .totalQuantity(100)
                .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Location code cannot be empty");
    }

}

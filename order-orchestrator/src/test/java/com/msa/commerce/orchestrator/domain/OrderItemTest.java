package com.msa.commerce.orchestrator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem 도메인 테스트")
class OrderItemTest {

    @Test
    @DisplayName("정상적인 OrderItem 생성")
    void createOrderItem_Success() {
        // given
        Long productId = 1L;
        String productName = "테스트 상품";
        String productSku = "TEST-001";
        Long productVariantId = 2L;
        String variantName = "빨간색 L 사이즈";
        Integer quantity = 2;
        BigDecimal unitPrice = new BigDecimal("10000.00");

        // when
        OrderItem orderItem = OrderItem.create(
            productId, productName, productSku,
            productVariantId, variantName, quantity, unitPrice
        );

        // then
        assertThat(orderItem.getOrderItemId()).isNotNull();
        assertThat(orderItem.getProductId()).isEqualTo(productId);
        assertThat(orderItem.getProductName()).isEqualTo(productName);
        assertThat(orderItem.getProductSku()).isEqualTo(productSku);
        assertThat(orderItem.getProductVariantId()).isEqualTo(productVariantId);
        assertThat(orderItem.getVariantName()).isEqualTo(variantName);
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(orderItem.getTotalPrice()).isEqualTo(new BigDecimal("20000.00"));
    }

    @Test
    @DisplayName("변형 없는 OrderItem 생성")
    void createOrderItemWithoutVariant_Success() {
        // given
        Long productId = 1L;
        String productName = "테스트 상품";
        String productSku = "TEST-001";
        Integer quantity = 1;
        BigDecimal unitPrice = new BigDecimal("15000.50");

        // when
        OrderItem orderItem = OrderItem.create(
            productId, productName, productSku,
            null, null, quantity, unitPrice
        );

        // then
        assertThat(orderItem.getOrderItemId()).isNotNull();
        assertThat(orderItem.getProductId()).isEqualTo(productId);
        assertThat(orderItem.getProductName()).isEqualTo(productName);
        assertThat(orderItem.getProductSku()).isEqualTo(productSku);
        assertThat(orderItem.getProductVariantId()).isNull();
        assertThat(orderItem.getVariantName()).isNull();
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(orderItem.getTotalPrice()).isEqualTo(new BigDecimal("15000.50"));
    }

    @Test
    @DisplayName("수량 업데이트")
    void updateQuantity_Success() {
        // given
        OrderItem orderItem = createValidOrderItem();
        Integer newQuantity = 5;

        // when
        orderItem.updateQuantity(newQuantity);

        // then
        assertThat(orderItem.getQuantity()).isEqualTo(newQuantity);
        assertThat(orderItem.getTotalPrice()).isEqualTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("단가 업데이트")
    void updateUnitPrice_Success() {
        // given
        OrderItem orderItem = createValidOrderItem();
        BigDecimal newUnitPrice = new BigDecimal("12000.00");

        // when
        orderItem.updateUnitPrice(newUnitPrice);

        // then
        assertThat(orderItem.getUnitPrice()).isEqualTo(newUnitPrice);
        assertThat(orderItem.getTotalPrice()).isEqualTo(new BigDecimal("24000.00"));
    }

    @Test
    @DisplayName("null 상품 ID로 생성 시 예외")
    void createOrderItemWithNullProductId_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(null, "상품명", "SKU001", null, null, 1, BigDecimal.TEN)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Product ID cannot be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("빈 상품명으로 생성 시 예외")
    void createOrderItemWithBlankProductName_ThrowsException(String productName) {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, productName, "SKU001", null, null, 1, BigDecimal.TEN)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Product name cannot be null or empty");
    }

    @Test
    @DisplayName("null 상품명으로 생성 시 예외")
    void createOrderItemWithNullProductName_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, null, "SKU001", null, null, 1, BigDecimal.TEN)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Product name cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("빈 SKU로 생성 시 예외")
    void createOrderItemWithBlankProductSku_ThrowsException(String productSku) {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, "상품명", productSku, null, null, 1, BigDecimal.TEN)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Product SKU cannot be null or empty");
    }

    @Test
    @DisplayName("null SKU로 생성 시 예외")
    void createOrderItemWithNullProductSku_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, "상품명", null, null, null, 1, BigDecimal.TEN)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Product SKU cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("잘못된 수량으로 생성 시 예외")
    void createOrderItemWithInvalidQuantity_ThrowsException(Integer quantity) {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, "상품명", "SKU001", null, null, quantity, BigDecimal.TEN)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Quantity must be greater than 0");
    }

    @Test
    @DisplayName("null 수량으로 생성 시 예외")
    void createOrderItemWithNullQuantity_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, "상품명", "SKU001", null, null, null, BigDecimal.TEN)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Quantity must be greater than 0");
    }

    @Test
    @DisplayName("음수 단가로 생성 시 예외")
    void createOrderItemWithNegativeUnitPrice_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, "상품명", "SKU001", null, null, 1, new BigDecimal("-100"))
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Unit price cannot be null or negative");
    }

    @Test
    @DisplayName("null 단가로 생성 시 예외")
    void createOrderItemWithNullUnitPrice_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            OrderItem.create(1L, "상품명", "SKU001", null, null, 1, null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Unit price cannot be null or negative");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("잘못된 수량으로 업데이트 시 예외")
    void updateQuantityWithInvalidValue_ThrowsException(Integer newQuantity) {
        // given
        OrderItem orderItem = createValidOrderItem();

        // when & then
        assertThatThrownBy(() -> orderItem.updateQuantity(newQuantity))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be greater than 0");
    }

    @Test
    @DisplayName("음수 단가로 업데이트 시 예외")
    void updateUnitPriceWithNegativeValue_ThrowsException() {
        // given
        OrderItem orderItem = createValidOrderItem();

        // when & then
        assertThatThrownBy(() -> orderItem.updateUnitPrice(new BigDecimal("-100")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unit price cannot be null or negative");
    }

    @Test
    @DisplayName("OrderItem equals 및 hashCode 테스트")
    void orderItemEqualsAndHashCode() {
        // given
        OrderItem orderItem1 = createValidOrderItem();
        OrderItem orderItem2 = createValidOrderItem();

        // when & then
        assertThat(orderItem1).isNotEqualTo(orderItem2);
        assertThat(orderItem1.hashCode()).isNotEqualTo(orderItem2.hashCode());
        assertThat(orderItem1).isEqualTo(orderItem1);
    }

    @Test
    @DisplayName("OrderItem toString 테스트")
    void orderItemToString() {
        // given
        OrderItem orderItem = createValidOrderItem();

        // when
        String toString = orderItem.toString();

        // then
        assertThat(toString)
            .contains("OrderItem(")
            .contains("productId=1")
            .contains("productName=테스트 상품")
            .contains("quantity=2")
            .contains("unitPrice=10000")
            .contains("totalPrice=20000");
    }

    private OrderItem createValidOrderItem() {
        return OrderItem.create(
            1L,
            "테스트 상품",
            "TEST-001",
            null,
            null,
            2,
            new BigDecimal("10000.00")
        );
    }
}
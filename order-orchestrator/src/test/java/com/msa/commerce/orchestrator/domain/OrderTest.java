package com.msa.commerce.orchestrator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order 도메인 테스트")
class OrderTest {

    @Test
    @DisplayName("정상적인 Order 생성")
    void createOrder_Success() {
        // given
        String orderNumber = "ORDER-001";
        Long customerId = 1L;
        Map<String, Object> shippingAddress = createValidShippingAddress();
        String sourceChannel = "WEB";

        // when
        Order order = Order.create(orderNumber, customerId, shippingAddress, sourceChannel);

        // then
        assertThat(order.getOrderId()).isNotNull();
        assertThat(order.getOrderNumber()).isEqualTo(orderNumber);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getSubtotalAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getTaxAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getShippingAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(order.getCurrency()).isEqualTo("KRW");
        assertThat(order.getShippingAddress()).containsAllEntriesOf(shippingAddress);
        assertThat(order.getSourceChannel()).isEqualTo(sourceChannel);
        assertThat(order.getOrderDate()).isNotNull();
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isNotNull();
        assertThat(order.getVersion()).isEqualTo(1L);
        assertThat(order.getOrderItems()).isEmpty();
    }

    @Test
    @DisplayName("소스 채널 없이 Order 생성 - 기본값 WEB")
    void createOrderWithoutSourceChannel_DefaultsToWeb() {
        // given
        String orderNumber = "ORDER-002";
        Long customerId = 1L;
        Map<String, Object> shippingAddress = createValidShippingAddress();

        // when
        Order order = Order.create(orderNumber, customerId, shippingAddress, null);

        // then
        assertThat(order.getSourceChannel()).isEqualTo("WEB");
    }

    @Test
    @DisplayName("OrderItem 추가")
    void addOrderItem_Success() {
        // given
        Order order = createValidOrder();
        OrderItem orderItem = createValidOrderItem();

        // when
        order.addOrderItem(orderItem);

        // then
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems()).contains(orderItem);
        assertThat(order.getSubtotalAmount()).isEqualTo(new BigDecimal("20000.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("20000.00"));
        assertThat(order.getTotalItemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("여러 OrderItem 추가")
    void addMultipleOrderItems_Success() {
        // given
        Order order = createValidOrder();
        OrderItem orderItem1 = createValidOrderItem();
        OrderItem orderItem2 = OrderItem.create(
            2L, "상품2", "SKU-002", null, null, 1, new BigDecimal("15000.00")
        );

        // when
        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);

        // then
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getSubtotalAmount()).isEqualTo(new BigDecimal("35000.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("35000.00"));
        assertThat(order.getTotalItemCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("OrderItem 제거")
    void removeOrderItem_Success() {
        // given
        Order order = createValidOrder();
        OrderItem orderItem1 = createValidOrderItem();
        OrderItem orderItem2 = OrderItem.create(
            2L, "상품2", "SKU-002", null, null, 1, new BigDecimal("15000.00")
        );
        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);

        // when
        order.removeOrderItem(orderItem1.getOrderItemId());

        // then
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getSubtotalAmount()).isEqualTo(new BigDecimal("15000.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("15000.00"));
        assertThat(order.getTotalItemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 확인")
    void confirmOrder_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());

        // when
        order.confirm();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 대기 상태로 변경")
    void markPaymentPending_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm();

        // when
        order.markPaymentPending();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
    }

    @Test
    @DisplayName("결제 완료 처리")
    void markPaymentCompleted_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm();
        order.markPaymentPending();

        // when
        order.markPaymentCompleted();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("처리 시작")
    void startProcessing_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm();
        order.markPaymentPending();
        order.markPaymentCompleted();

        // when
        order.startProcessing();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    @DisplayName("배송 처리")
    void markShipped_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm();
        order.markPaymentPending();
        order.markPaymentCompleted();
        order.startProcessing();

        // when
        order.markShipped();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getShippedAt()).isNotNull();
    }

    @Test
    @DisplayName("배송 완료")
    void markDelivered_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm();
        order.markPaymentPending();
        order.markPaymentCompleted();
        order.startProcessing();
        order.markShipped();

        // when
        order.markDelivered();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getDeliveredAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 취소")
    void cancelOrder_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());

        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("배송비 업데이트")
    void updateShippingAmount_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        BigDecimal shippingAmount = new BigDecimal("3000.00");

        // when
        order.updateShippingAmount(shippingAmount);

        // then
        assertThat(order.getShippingAmount()).isEqualTo(shippingAmount);
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("23000.00"));
    }

    @Test
    @DisplayName("세금 업데이트")
    void updateTaxAmount_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        BigDecimal taxAmount = new BigDecimal("2000.00");

        // when
        order.updateTaxAmount(taxAmount);

        // then
        assertThat(order.getTaxAmount()).isEqualTo(taxAmount);
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("22000.00"));
    }

    @Test
    @DisplayName("할인 금액 업데이트")
    void updateDiscountAmount_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        BigDecimal discountAmount = new BigDecimal("1000.00");

        // when
        order.updateDiscountAmount(discountAmount);

        // then
        assertThat(order.getDiscountAmount()).isEqualTo(discountAmount);
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("19000.00"));
    }

    @Test
    @DisplayName("복합 금액 업데이트")
    void updateAllAmounts_Success() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem()); // 20,000원

        // when
        order.updateTaxAmount(new BigDecimal("2000.00"));      // +2,000원
        order.updateShippingAmount(new BigDecimal("3000.00")); // +3,000원
        order.updateDiscountAmount(new BigDecimal("1000.00")); // -1,000원

        // then
        assertThat(order.getSubtotalAmount()).isEqualTo(new BigDecimal("20000.00"));
        assertThat(order.getTaxAmount()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(order.getShippingAmount()).isEqualTo(new BigDecimal("3000.00"));
        assertThat(order.getDiscountAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("24000.00")); // 20,000 + 2,000 + 3,000 - 1,000
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("빈 주문번호로 생성 시 예외")
    void createOrderWithBlankOrderNumber_ThrowsException(String orderNumber) {
        // when & then
        assertThatThrownBy(() ->
            Order.create(orderNumber, 1L, createValidShippingAddress(), "WEB")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Order number cannot be null or empty");
    }

    @Test
    @DisplayName("null 주문번호로 생성 시 예외")
    void createOrderWithNullOrderNumber_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            Order.create(null, 1L, createValidShippingAddress(), "WEB")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Order number cannot be null or empty");
    }

    @Test
    @DisplayName("null 고객 ID로 생성 시 예외")
    void createOrderWithNullCustomerId_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            Order.create("ORDER-001", null, createValidShippingAddress(), "WEB")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Customer ID cannot be null");
    }

    @Test
    @DisplayName("null 배송주소로 생성 시 예외")
    void createOrderWithNullShippingAddress_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            Order.create("ORDER-001", 1L, null, "WEB")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Shipping address cannot be null or empty");
    }

    @Test
    @DisplayName("빈 배송주소로 생성 시 예외")
    void createOrderWithEmptyShippingAddress_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            Order.create("ORDER-001", 1L, new HashMap<>(), "WEB")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Shipping address cannot be null or empty");
    }

    @Test
    @DisplayName("null OrderItem 추가 시 예외")
    void addNullOrderItem_ThrowsException() {
        // given
        Order order = createValidOrder();

        // when & then
        assertThatThrownBy(() -> order.addOrderItem(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Order item cannot be null");
    }

    @Test
    @DisplayName("처리 중 상태에서 아이템 추가 시 예외")
    void addOrderItemWhenProcessing_ThrowsException() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm();
        order.markPaymentPending();
        order.markPaymentCompleted();
        OrderItem newItem = OrderItem.create(2L, "상품2", "SKU-002", null, null, 1, BigDecimal.TEN);

        // when & then
        assertThatThrownBy(() -> order.addOrderItem(newItem))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot add items to order in status: PAID");
    }

    @Test
    @DisplayName("아이템 없이 주문 확인 시 예외")
    void confirmOrderWithoutItems_ThrowsException() {
        // given
        Order order = createValidOrder();

        // when & then
        assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot confirm order with no items");
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 주문 확인 시 예외")
    void confirmNonPendingOrder_ThrowsException() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm(); // 이미 CONFIRMED 상태

        // when & then
        assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Order must be in PENDING status to be confirmed");
    }

    @Test
    @DisplayName("처리 중 상태에서 취소 시 예외")
    void cancelProcessingOrder_ThrowsException() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());
        order.confirm();
        order.markPaymentPending();
        order.markPaymentCompleted();

        // when & then
        assertThatThrownBy(order::cancel)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Order cannot be cancelled in status: PAID");
    }

    @Test
    @DisplayName("음수 배송비 업데이트 시 예외")
    void updateShippingAmountWithNegativeValue_ThrowsException() {
        // given
        Order order = createValidOrder();

        // when & then
        assertThatThrownBy(() -> order.updateShippingAmount(new BigDecimal("-100")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Shipping amount cannot be null or negative");
    }

    @Test
    @DisplayName("Order equals 및 hashCode 테스트")
    void orderEqualsAndHashCode() {
        // given
        Order order1 = createValidOrder();
        Order order2 = createValidOrder();

        // when & then
        assertThat(order1).isNotEqualTo(order2);
        assertThat(order1.hashCode()).isNotEqualTo(order2.hashCode());
        assertThat(order1).isEqualTo(order1);
    }

    @Test
    @DisplayName("Order toString 테스트")
    void orderToString() {
        // given
        Order order = createValidOrder();
        order.addOrderItem(createValidOrderItem());

        // when
        String toString = order.toString();

        // then
        assertThat(toString)
            .contains("Order(")
            .contains("orderId=")
            .contains("orderNumber=ORDER-TEST")
            .contains("customerId=1")
            .contains("status=PENDING")
            .contains("totalAmount=20000.00")
            .contains("currency=KRW");
    }

    // Helper methods
    private Order createValidOrder() {
        return Order.create(
            "ORDER-TEST",
            1L,
            createValidShippingAddress(),
            "WEB"
        );
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

    private Map<String, Object> createValidShippingAddress() {
        Map<String, Object> address = new HashMap<>();
        address.put("recipient", "홍길동");
        address.put("phone", "010-1234-5678");
        address.put("addressLine1", "서울시 강남구 테헤란로 123");
        address.put("city", "서울시");
        address.put("postalCode", "06234");
        return address;
    }
}
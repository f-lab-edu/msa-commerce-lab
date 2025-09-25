package com.msa.commerce.orchestrator.adapter.out.persistence;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderJpaEntity 테스트")
class OrderJpaEntityTest {

    @Test
    @DisplayName("Order 도메인에서 JPA 엔티티 생성")
    void fromOrder_Success() {
        // given
        Order order = createValidOrder();

        // when
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        // then
        assertThat(entity.getOrderUuid()).isEqualTo(order.getOrderId());
        assertThat(entity.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(entity.getUserId()).isEqualTo(order.getCustomerId());
        assertThat(entity.getStatus()).isEqualTo(order.getStatus());
        assertThat(entity.getSubtotalAmount()).isEqualTo(order.getSubtotalAmount());
        assertThat(entity.getTaxAmount()).isEqualTo(order.getTaxAmount());
        assertThat(entity.getShippingAmount()).isEqualTo(order.getShippingAmount());
        assertThat(entity.getDiscountAmount()).isEqualTo(order.getDiscountAmount());
        assertThat(entity.getTotalAmount()).isEqualTo(order.getTotalAmount());
        assertThat(entity.getCurrency()).isEqualTo(order.getCurrency());
        assertThat(entity.getShippingAddress()).containsAllEntriesOf(order.getShippingAddress());
        assertThat(entity.getSourceChannel()).isEqualTo(order.getSourceChannel());
        assertThat(entity.getVersion()).isEqualTo(order.getVersion());
    }

    @Test
    @DisplayName("Order 도메인으로 JPA 엔티티 업데이트")
    void updateFromOrder_Success() {
        // given
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        // 주문에 아이템을 추가하고 상태 변경
        OrderItem orderItem = OrderItem.create(
            1L, "테스트 상품", "TEST-001", null, null, 1, new BigDecimal("10000.00")
        );
        order.addOrderItem(orderItem);
        order.confirm();
        order.updateTaxAmount(new BigDecimal("1000.00"));

        // when
        entity.updateFrom(order);

        // then
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(entity.getTaxAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(entity.getConfirmedAt()).isEqualTo(order.getConfirmedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(order.getUpdatedAt());
    }

    @Test
    @DisplayName("도메인 변환 미구현 예외")
    void toDomain_ThrowsUnsupportedOperationException() {
        // given
        Order order = createValidOrder();
        OrderJpaEntity entity = OrderJpaEntity.from(order);

        // when & then
        assertThatThrownBy(entity::toDomain)
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Domain conversion not yet implemented - requires proper hydration strategy");
    }

    private Order createValidOrder() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("recipient", "홍길동");
        shippingAddress.put("phone", "010-1234-5678");
        shippingAddress.put("addressLine1", "서울시 강남구");
        shippingAddress.put("postalCode", "06234");

        return Order.create(
            "ORDER-TEST-001",
            1L,
            shippingAddress,
            "WEB"
        );
    }
}
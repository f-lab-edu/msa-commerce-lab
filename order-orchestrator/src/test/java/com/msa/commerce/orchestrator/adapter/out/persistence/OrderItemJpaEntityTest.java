package com.msa.commerce.orchestrator.adapter.out.persistence;

import com.msa.commerce.orchestrator.domain.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItemJpaEntity 테스트")
class OrderItemJpaEntityTest {

    @Test
    @DisplayName("OrderItem 도메인에서 JPA 엔티티 생성")
    void fromOrderItem_Success() {
        // given
        OrderItem orderItem = createValidOrderItem();
        OrderJpaEntity orderEntity = new OrderJpaEntity();

        // when
        OrderItemJpaEntity entity = OrderItemJpaEntity.from(orderItem, orderEntity);

        // then
        assertThat(entity.getOrder()).isEqualTo(orderEntity);
        assertThat(entity.getProductId()).isEqualTo(orderItem.getProductId());
        assertThat(entity.getProductVariantId()).isEqualTo(orderItem.getProductVariantId());
        assertThat(entity.getProductName()).isEqualTo(orderItem.getProductName());
        assertThat(entity.getProductSku()).isEqualTo(orderItem.getProductSku());
        assertThat(entity.getVariantName()).isEqualTo(orderItem.getVariantName());
        assertThat(entity.getQuantity()).isEqualTo(orderItem.getQuantity());
        assertThat(entity.getUnitPrice()).isEqualTo(orderItem.getUnitPrice());
        assertThat(entity.getTotalPrice()).isEqualTo(orderItem.getTotalPrice());
    }

    @Test
    @DisplayName("OrderItem 도메인으로 JPA 엔티티 업데이트")
    void updateFromOrderItem_Success() {
        // given
        OrderItem originalOrderItem = createValidOrderItem();
        OrderJpaEntity orderEntity = new OrderJpaEntity();
        OrderItemJpaEntity entity = OrderItemJpaEntity.from(originalOrderItem, orderEntity);

        // 수량과 가격을 변경한 새로운 OrderItem 생성
        OrderItem updatedOrderItem = OrderItem.builder()
            .orderItemId(originalOrderItem.getOrderItemId())
            .productId(originalOrderItem.getProductId())
            .productVariantId(originalOrderItem.getProductVariantId())
            .productName(originalOrderItem.getProductName())
            .productSku(originalOrderItem.getProductSku())
            .variantName(originalOrderItem.getVariantName())
            .quantity(10)
            .unitPrice(new BigDecimal("15000.0000"))
            .totalPrice(new BigDecimal("150000.0000"))
            .build();

        // when
        entity.updateFrom(updatedOrderItem);

        // then
        assertThat(entity.getQuantity()).isEqualTo(10);
        assertThat(entity.getUnitPrice()).isEqualByComparingTo(new BigDecimal("15000.0000"));
        assertThat(entity.getTotalPrice()).isEqualByComparingTo(new BigDecimal("150000.0000"));
    }

    private OrderItem createValidOrderItem() {
        return OrderItem.create(
            1L,
            "테스트 상품",
            "TEST-001",
            2L,
            "빨간색 L",
            2,
            new BigDecimal("10000.00")
        );
    }
}
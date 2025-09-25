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
    @DisplayName("JPA 엔티티에서 OrderItem 도메인 생성")
    void toDomain_Success() {
        // given
        OrderItem originalOrderItem = createValidOrderItem();
        OrderJpaEntity orderEntity = new OrderJpaEntity();
        OrderItemJpaEntity entity = OrderItemJpaEntity.from(originalOrderItem, orderEntity);

        // when
        OrderItem domainOrderItem = entity.toDomain();

        // then
        assertThat(domainOrderItem.getProductId()).isEqualTo(originalOrderItem.getProductId());
        assertThat(domainOrderItem.getProductName()).isEqualTo(originalOrderItem.getProductName());
        assertThat(domainOrderItem.getProductSku()).isEqualTo(originalOrderItem.getProductSku());
        assertThat(domainOrderItem.getProductVariantId()).isEqualTo(originalOrderItem.getProductVariantId());
        assertThat(domainOrderItem.getVariantName()).isEqualTo(originalOrderItem.getVariantName());
        assertThat(domainOrderItem.getQuantity()).isEqualTo(originalOrderItem.getQuantity());
        assertThat(domainOrderItem.getUnitPrice()).isEqualTo(originalOrderItem.getUnitPrice());
        assertThat(domainOrderItem.getTotalPrice()).isEqualTo(originalOrderItem.getTotalPrice());
    }

    @Test
    @DisplayName("OrderItem 도메인으로 JPA 엔티티 업데이트")
    void updateFromOrderItem_Success() {
        // given
        OrderItem orderItem = createValidOrderItem();
        OrderJpaEntity orderEntity = new OrderJpaEntity();
        OrderItemJpaEntity entity = OrderItemJpaEntity.from(orderItem, orderEntity);

        // 수량과 단가 변경
        orderItem.updateQuantity(5);
        orderItem.updateUnitPrice(new BigDecimal("12000.00"));

        // when
        entity.updateFrom(orderItem);

        // then
        assertThat(entity.getQuantity()).isEqualTo(5);
        assertThat(entity.getUnitPrice()).isEqualTo(new BigDecimal("12000.00"));
        assertThat(entity.getTotalPrice()).isEqualTo(new BigDecimal("60000.00"));
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
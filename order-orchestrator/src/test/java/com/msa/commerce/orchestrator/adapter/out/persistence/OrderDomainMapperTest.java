package com.msa.commerce.orchestrator.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

@DisplayName("OrderDomainMapper 테스트")
class OrderDomainMapperTest {

    private final OrderDomainMapper mapper = Mappers.getMapper(OrderDomainMapper.class);

    @Test
    @DisplayName("OrderItem 도메인 변환")
    void toOrderItemDomain_Success() {
        // given
        OrderJpaEntity orderEntity = new OrderJpaEntity();
        OrderItem originalOrderItem = createValidOrderItem();
        OrderItemJpaEntity entity = OrderItemJpaEntity.from(originalOrderItem, orderEntity);

        // when
        OrderItem domainOrderItem = mapper.toOrderItemDomain(entity);

        // then
        assertThat(domainOrderItem.getProductId()).isEqualTo(originalOrderItem.getProductId());
        assertThat(domainOrderItem.getProductName()).isEqualTo(originalOrderItem.getProductName());
        assertThat(domainOrderItem.getProductSku()).isEqualTo(originalOrderItem.getProductSku());
        assertThat(domainOrderItem.getQuantity()).isEqualTo(originalOrderItem.getQuantity());
        assertThat(domainOrderItem.getUnitPrice()).isEqualTo(originalOrderItem.getUnitPrice());
        assertThat(domainOrderItem.getTotalPrice()).isEqualTo(originalOrderItem.getTotalPrice());
    }

    @Test
    @DisplayName("OrderItem 리스트 도메인 변환")
    void toOrderItemDomainList_Success() {
        // given
        OrderJpaEntity orderEntity = new OrderJpaEntity();
        OrderItem originalOrderItem1 = createValidOrderItem();
        OrderItem originalOrderItem2 = OrderItem.create(
            2L, "상품2", "SKU-002", null, null, 1, new BigDecimal("15000.00")
        );

        List<OrderItemJpaEntity> entities = List.of(
            OrderItemJpaEntity.from(originalOrderItem1, orderEntity),
            OrderItemJpaEntity.from(originalOrderItem2, orderEntity)
        );

        // when
        List<OrderItem> domainItems = mapper.toOrderItemDomainList(entities);

        // then
        assertThat(domainItems).hasSize(2);
        assertThat(domainItems.get(0).getProductId()).isEqualTo(originalOrderItem1.getProductId());
        assertThat(domainItems.get(1).getProductId()).isEqualTo(originalOrderItem2.getProductId());
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

package com.msa.commerce.orchestrator.adapter.out.kafka.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderEvent;
import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderEventType;
import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderItemEvent;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;

class OrderEventMapperTest {

    private final OrderEventMapper mapper = Mappers.getMapper(OrderEventMapper.class);

    @Test
    void shouldMapOrderToOrderCreatedEvent() {
        OrderItem orderItem = OrderItem.builder()
            .productId(1001L)
            .productName("테스트 상품")
            .productSku("TEST-SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(10000))
            .totalPrice(BigDecimal.valueOf(20000))
            .build();

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");
        shippingAddress.put("street", "강남대로 123");

        Order order = Order.builder()
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .status(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(20000))
            .orderDate(LocalDateTime.of(2024, 1, 15, 10, 30))
            .shippingAddress(shippingAddress)
            .orderItems(List.of(orderItem))
            .build();

        OrderEvent event = mapper.toOrderCreatedEvent(order);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventType()).isEqualTo(OrderEventType.ORDER_CREATED);
        assertThat(event.orderId()).isEqualTo(order.getOrderId());
        assertThat(event.customerId()).isEqualTo(12345L);
        assertThat(event.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(event.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(event.orderDate()).isEqualTo(order.getOrderDate());
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.orderItems()).hasSize(1);

        OrderItemEvent itemEvent = event.orderItems().get(0);
        assertThat(itemEvent.productId()).isEqualTo(1001L);
        assertThat(itemEvent.productName()).isEqualTo("테스트 상품");
        assertThat(itemEvent.quantity()).isEqualTo(2);
        assertThat(itemEvent.unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(itemEvent.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    void shouldMapOrderToOrderStatusChangedEvent() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");

        Order order = Order.builder()
            .orderNumber("ORD-20240115-002")
            .customerId(12345L)
            .status(OrderStatus.CONFIRMED)
            .totalAmount(BigDecimal.valueOf(30000))
            .orderDate(LocalDateTime.of(2024, 1, 15, 10, 30))
            .shippingAddress(shippingAddress)
            .orderItems(List.of())
            .build();

        OrderEvent event = mapper.toOrderStatusChangedEvent(order);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventType()).isEqualTo(OrderEventType.ORDER_STATUS_CHANGED);
        assertThat(event.orderId()).isEqualTo(order.getOrderId());
        assertThat(event.customerId()).isEqualTo(12345L);
        assertThat(event.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(event.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(event.orderDate()).isEqualTo(order.getOrderDate());
        assertThat(event.timestamp()).isNotNull();
        assertThat(event.orderItems()).isEmpty();
    }

    @Test
    void shouldMapOrderItemToOrderItemEvent() {
        OrderItem orderItem = OrderItem.builder()
            .productId(2001L)
            .productName("상품명")
            .productSku("SKU-001")
            .quantity(3)
            .unitPrice(BigDecimal.valueOf(5000))
            .totalPrice(BigDecimal.valueOf(15000))
            .build();

        OrderItemEvent itemEvent = mapper.toOrderItemEvent(orderItem);

        assertThat(itemEvent.orderItemId()).isEqualTo(orderItem.getOrderItemId());
        assertThat(itemEvent.productId()).isEqualTo(2001L);
        assertThat(itemEvent.productName()).isEqualTo("상품명");
        assertThat(itemEvent.quantity()).isEqualTo(3);
        assertThat(itemEvent.unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(itemEvent.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    void shouldHandleEmptyOrderItems() {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", "서울");

        Order order = Order.builder()
            .orderNumber("ORD-20240115-003")
            .customerId(12345L)
            .status(OrderStatus.CANCELLED)
            .totalAmount(BigDecimal.ZERO)
            .orderDate(LocalDateTime.now())
            .shippingAddress(shippingAddress)
            .orderItems(List.of())
            .build();

        OrderEvent event = mapper.toOrderCreatedEvent(order);

        assertThat(event.orderItems()).isEmpty();
    }
}

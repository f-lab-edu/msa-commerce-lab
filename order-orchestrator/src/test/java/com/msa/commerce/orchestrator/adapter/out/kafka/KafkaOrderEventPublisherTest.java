package com.msa.commerce.orchestrator.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderEvent;
import com.msa.commerce.orchestrator.adapter.out.kafka.event.OrderEventType;
import com.msa.commerce.orchestrator.adapter.out.kafka.mapper.OrderEventMapper;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
class KafkaOrderEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OrderEventMapper orderEventMapper;

    @InjectMocks
    private KafkaOrderEventPublisher kafkaOrderEventPublisher;

    @Captor
    private ArgumentCaptor<OrderEvent> eventCaptor;

    private Order testOrder;

    @BeforeEach
    void setUp() {
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

        testOrder = Order.builder()
            .orderId(java.util.UUID.randomUUID())
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .status(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(20000))
            .orderDate(LocalDateTime.now())
            .shippingAddress(shippingAddress)
            .orderItems(List.of(orderItem))
            .build();
    }

    @Test
    void shouldPublishOrderCreatedEvent() {
        OrderEvent orderEvent = OrderEvent.builder()
            .eventId("test-event-id")
            .eventType(OrderEventType.ORDER_CREATED)
            .orderId(testOrder.getOrderId())
            .customerId(testOrder.getCustomerId())
            .orderStatus(testOrder.getStatus())
            .totalAmount(testOrder.getTotalAmount())
            .orderDate(testOrder.getOrderDate())
            .timestamp(LocalDateTime.now())
            .orderItems(List.of())
            .build();

        when(orderEventMapper.toOrderCreatedEvent(testOrder)).thenReturn(orderEvent);
        when(kafkaTemplate.send(anyString(), anyString(), any(OrderEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        kafkaOrderEventPublisher.publishOrderCreatedEvent(testOrder);

        verify(orderEventMapper, times(1)).toOrderCreatedEvent(testOrder);
        verify(kafkaTemplate, times(1)).send(
            eq("order-events"),
            eq(testOrder.getOrderId().toString()),
            eventCaptor.capture()
        );

        OrderEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.eventType()).isEqualTo(OrderEventType.ORDER_CREATED);
        assertThat(capturedEvent.orderId()).isEqualTo(testOrder.getOrderId());
    }

    @Test
    void shouldPublishOrderStatusChangedEvent() {
        OrderEvent orderEvent = OrderEvent.builder()
            .eventId("test-event-id-2")
            .eventType(OrderEventType.ORDER_STATUS_CHANGED)
            .orderId(testOrder.getOrderId())
            .customerId(testOrder.getCustomerId())
            .orderStatus(OrderStatus.CONFIRMED)
            .totalAmount(testOrder.getTotalAmount())
            .orderDate(testOrder.getOrderDate())
            .timestamp(LocalDateTime.now())
            .orderItems(List.of())
            .build();

        when(orderEventMapper.toOrderStatusChangedEvent(testOrder)).thenReturn(orderEvent);
        when(kafkaTemplate.send(anyString(), anyString(), any(OrderEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        kafkaOrderEventPublisher.publishOrderStatusChangedEvent(testOrder);

        verify(orderEventMapper, times(1)).toOrderStatusChangedEvent(testOrder);
        verify(kafkaTemplate, times(1)).send(
            eq("order-events"),
            eq(testOrder.getOrderId().toString()),
            eventCaptor.capture()
        );

        OrderEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.eventType()).isEqualTo(OrderEventType.ORDER_STATUS_CHANGED);
    }

    @Test
    void shouldUseOrderIdAsKafkaKey() {
        OrderEvent orderEvent = OrderEvent.builder()
            .eventId("test-event-id")
            .eventType(OrderEventType.ORDER_CREATED)
            .orderId(testOrder.getOrderId())
            .customerId(testOrder.getCustomerId())
            .orderStatus(testOrder.getStatus())
            .totalAmount(testOrder.getTotalAmount())
            .orderDate(testOrder.getOrderDate())
            .timestamp(LocalDateTime.now())
            .orderItems(List.of())
            .build();

        when(orderEventMapper.toOrderCreatedEvent(testOrder)).thenReturn(orderEvent);
        when(kafkaTemplate.send(anyString(), anyString(), any(OrderEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        kafkaOrderEventPublisher.publishOrderCreatedEvent(testOrder);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any(OrderEvent.class));

        assertThat(keyCaptor.getValue()).isEqualTo(testOrder.getOrderId().toString());
    }
}

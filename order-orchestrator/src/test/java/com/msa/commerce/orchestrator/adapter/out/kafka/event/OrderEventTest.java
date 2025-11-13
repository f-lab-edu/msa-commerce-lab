package com.msa.commerce.orchestrator.adapter.out.kafka.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.orchestrator.domain.OrderStatus;

class OrderEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @Test
    void shouldCreateOrderCreatedEvent() {
        UUID orderId = UUID.randomUUID();
        Long customerId = 12345L;
        LocalDateTime orderDate = LocalDateTime.now();
        LocalDateTime timestamp = LocalDateTime.now();

        OrderItemEvent orderItem = new OrderItemEvent(
            UUID.randomUUID(),
            1001L,
            "테스트 상품",
            2,
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(20000)
        );

        OrderEvent event = OrderEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(OrderEventType.ORDER_CREATED)
            .orderId(orderId)
            .customerId(customerId)
            .orderStatus(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(20000))
            .orderDate(orderDate)
            .timestamp(timestamp)
            .orderItems(List.of(orderItem))
            .build();

        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventType()).isEqualTo(OrderEventType.ORDER_CREATED);
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.customerId()).isEqualTo(customerId);
        assertThat(event.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(event.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(event.orderDate()).isEqualTo(orderDate);
        assertThat(event.timestamp()).isEqualTo(timestamp);
        assertThat(event.orderItems()).hasSize(1);
    }

    @Test
    void shouldCreateOrderStatusChangedEvent() {
        UUID orderId = UUID.randomUUID();
        Long customerId = 12345L;
        LocalDateTime orderDate = LocalDateTime.now();
        LocalDateTime timestamp = LocalDateTime.now();

        OrderEvent event = OrderEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(OrderEventType.ORDER_STATUS_CHANGED)
            .orderId(orderId)
            .customerId(customerId)
            .orderStatus(OrderStatus.CONFIRMED)
            .totalAmount(BigDecimal.valueOf(20000))
            .orderDate(orderDate)
            .timestamp(timestamp)
            .orderItems(List.of())
            .build();

        assertThat(event.eventType()).isEqualTo(OrderEventType.ORDER_STATUS_CHANGED);
        assertThat(event.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldSerializeToJson() throws JsonProcessingException {
        OrderItemEvent orderItem = new OrderItemEvent(
            UUID.randomUUID(),
            1001L,
            "테스트 상품",
            2,
            BigDecimal.valueOf(10000),
            BigDecimal.valueOf(20000)
        );

        OrderEvent event = OrderEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(OrderEventType.ORDER_CREATED)
            .orderId(UUID.randomUUID())
            .customerId(12345L)
            .orderStatus(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(20000))
            .orderDate(LocalDateTime.now())
            .timestamp(LocalDateTime.now())
            .orderItems(List.of(orderItem))
            .build();

        String json = objectMapper.writeValueAsString(event);

        assertThat(json).isNotNull();
        assertThat(json).contains("ORDER_CREATED");
        assertThat(json).contains("PENDING");
        assertThat(json).contains("12345");
        assertThat(json).contains("테스트 상품");
    }

    @Test
    void shouldDeserializeFromJson() throws JsonProcessingException {
        String json = """
            {
                "eventId": "test-event-id",
                "eventType": "ORDER_CREATED",
                "orderId": "550e8400-e29b-41d4-a716-446655440000",
                "customerId": 12345,
                "orderStatus": "PENDING",
                "totalAmount": 20000,
                "orderDate": "2024-01-15T10:30:00",
                "timestamp": "2024-01-15T10:30:00",
                "orderItems": [
                    {
                        "orderItemId": "550e8400-e29b-41d4-a716-446655440001",
                        "productId": 1001,
                        "productName": "테스트 상품",
                        "quantity": 2,
                        "unitPrice": 10000,
                        "totalPrice": 20000
                    }
                ]
            }
            """;

        OrderEvent event = objectMapper.readValue(json, OrderEvent.class);

        assertThat(event.eventId()).isEqualTo("test-event-id");
        assertThat(event.eventType()).isEqualTo(OrderEventType.ORDER_CREATED);
        assertThat(event.customerId()).isEqualTo(12345L);
        assertThat(event.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(event.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(event.orderItems()).hasSize(1);
        assertThat(event.orderItems().get(0).productName()).isEqualTo("테스트 상품");
    }

    @Test
    void shouldHandleEmptyOrderItems() {
        OrderEvent event = OrderEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(OrderEventType.ORDER_STATUS_CHANGED)
            .orderId(UUID.randomUUID())
            .customerId(12345L)
            .orderStatus(OrderStatus.CANCELLED)
            .totalAmount(BigDecimal.ZERO)
            .orderDate(LocalDateTime.now())
            .timestamp(LocalDateTime.now())
            .orderItems(List.of())
            .build();

        assertThat(event.orderItems()).isEmpty();
    }
}

package com.msa.commerce.orchestrator.adapter.out.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.Builder;

@Builder
public record OrderEvent(
    String eventId,
    OrderEventType eventType,
    UUID orderId,
    Long customerId,
    OrderStatus orderStatus,
    BigDecimal totalAmount,
    LocalDateTime orderDate,
    LocalDateTime timestamp,
    List<OrderItemEvent> orderItems
) {
}

package com.msa.commerce.orchestrator.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.Builder;

@Builder
public record OrderResponse(
    UUID orderId,
    String orderNumber,
    Long customerId,
    OrderStatus status,
    BigDecimal totalAmount,
    String currency,
    LocalDateTime orderDate,
    List<OrderItemResponse> orderItems
) {
}

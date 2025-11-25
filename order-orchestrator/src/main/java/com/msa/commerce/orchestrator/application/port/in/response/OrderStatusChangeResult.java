package com.msa.commerce.orchestrator.application.port.in.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public record OrderStatusChangeResult(
    UUID orderId,
    String orderNumber,
    OrderStatus previousStatus,
    OrderStatus currentStatus,
    LocalDateTime changedAt
) {

}

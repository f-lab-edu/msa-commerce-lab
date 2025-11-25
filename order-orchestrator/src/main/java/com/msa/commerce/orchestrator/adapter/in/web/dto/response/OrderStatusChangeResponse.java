package com.msa.commerce.orchestrator.adapter.in.web.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public record OrderStatusChangeResponse(
    UUID orderId,
    String orderNumber,
    OrderStatus previousStatus,
    OrderStatus currentStatus,
    LocalDateTime changedAt
) {

}

package com.msa.commerce.orchestrator.application.port.in;

import java.util.UUID;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.Builder;

@Builder
public record UpdateOrderStatusCommand(
    UUID orderId,
    OrderStatus newStatus
) {

}

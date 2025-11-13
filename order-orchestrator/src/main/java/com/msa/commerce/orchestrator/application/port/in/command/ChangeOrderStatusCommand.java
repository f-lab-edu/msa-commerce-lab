package com.msa.commerce.orchestrator.application.port.in.command;

import java.util.UUID;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public record ChangeOrderStatusCommand(
    UUID orderId,
    OrderStatus newStatus,
    String reason
) {

    public ChangeOrderStatusCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
    }

}

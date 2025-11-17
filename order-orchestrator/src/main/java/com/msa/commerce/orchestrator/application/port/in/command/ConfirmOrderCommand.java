package com.msa.commerce.orchestrator.application.port.in.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ConfirmOrderCommand(@NotNull(message = "Order ID is required") UUID orderId) {

}

package com.msa.commerce.orchestrator.application.port.in.command;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CancelOrderCommand(@NotNull(message = "Order ID is required") UUID orderId,
                                 @NotBlank(message = "Cancellation reason is required") String reason) {

}

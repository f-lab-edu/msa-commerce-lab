package com.msa.commerce.payment.application.port.in.command;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CancelPaymentCommand(

    @NotNull(message = "Payment ID is required")
    UUID paymentId,

    @NotBlank(message = "Cancel reason is required")
    @Size(max = 500, message = "Cancel reason cannot exceed 500 characters")
    String reason,

    String correlationId
) {

}

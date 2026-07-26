package com.msa.commerce.payment.application.port.in.command;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RefundPaymentCommand(

    @NotNull(message = "Payment ID is required")
    UUID paymentId,

    // 생략하면 잔여 환불 가능 금액 전액을 환불한다
    @DecimalMin(value = "0.0001", message = "Refund amount must be greater than 0")
    @Digits(integer = 8, fraction = 4, message = "Invalid refund amount format")
    BigDecimal amount,

    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
    String reason,

    String correlationId
) {

}

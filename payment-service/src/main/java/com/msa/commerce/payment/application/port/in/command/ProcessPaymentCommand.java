package com.msa.commerce.payment.application.port.in.command;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import com.msa.commerce.payment.domain.PaymentMethod;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ProcessPaymentCommand(

    @NotNull(message = "Order ID is required")
    UUID orderId,

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    Long customerId,

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0001", message = "Payment amount must be greater than 0")
    @DecimalMax(value = "99999999.9999", message = "Payment amount cannot exceed 99,999,999.9999")
    @Digits(integer = 8, fraction = 4, message = "Invalid payment amount format")
    BigDecimal amount,

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO 4217 code")
    String currency,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    Map<String, Object> paymentDetails,

    // 사가 전 구간을 추적하기 위한 값. 없으면 이벤트 발행 시점에 새로 만든다.
    String correlationId
) {

}

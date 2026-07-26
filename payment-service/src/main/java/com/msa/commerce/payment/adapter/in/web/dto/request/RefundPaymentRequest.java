package com.msa.commerce.payment.adapter.in.web.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefundPaymentRequest(

    // 생략하면 잔여 환불 가능 금액 전액을 환불한다
    @DecimalMin(value = "0.0001", message = "Refund amount must be greater than 0")
    @Digits(integer = 8, fraction = 4, message = "Invalid refund amount format")
    BigDecimal amount,

    @NotBlank(message = "Refund reason is required")
    @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
    String reason
) {

}

package com.msa.commerce.payment.application.port.in.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

import lombok.Builder;

@Builder
public record PaymentResponse(
    UUID paymentId,
    UUID orderId,
    Long customerId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    PaymentMethod paymentMethod,
    String paymentProvider,
    String gatewayTransactionId,
    String approvalNumber,
    String failureCode,
    String failureReason,
    String cancelReason,
    BigDecimal refundAmount,
    String refundReason,
    LocalDateTime authorizedAt,
    LocalDateTime capturedAt,
    LocalDateTime cancelledAt,
    LocalDateTime failedAt,
    LocalDateTime refundedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}

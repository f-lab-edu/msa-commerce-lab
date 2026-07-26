package com.msa.commerce.payment.application.service.mapper;

import org.springframework.stereotype.Component;

import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.domain.Payment;

@Component
public class PaymentResponseMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
            .paymentId(payment.getPaymentId())
            .orderId(payment.getOrderId())
            .customerId(payment.getCustomerId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .status(payment.getStatus())
            .paymentMethod(payment.getPaymentMethod())
            .paymentProvider(payment.getPaymentProvider())
            .gatewayTransactionId(payment.getGatewayTransactionId())
            .approvalNumber(payment.getApprovalNumber())
            .failureCode(payment.getFailureCode())
            .failureReason(payment.getFailureReason())
            .authorizedAt(payment.getAuthorizedAt())
            .capturedAt(payment.getCapturedAt())
            .cancelledAt(payment.getCancelledAt())
            .failedAt(payment.getFailedAt())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
    }

}

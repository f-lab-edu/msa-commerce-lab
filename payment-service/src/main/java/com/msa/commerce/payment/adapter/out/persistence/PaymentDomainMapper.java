package com.msa.commerce.payment.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.msa.commerce.payment.domain.Payment;

@Component
public class PaymentDomainMapper {

    public Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Payment.builder()
            .id(entity.getId())
            .paymentId(entity.getPaymentId())
            .orderId(entity.getOrderId())
            .customerId(entity.getCustomerId())
            .amount(entity.getAmount())
            .currency(entity.getCurrency())
            .status(entity.getStatus())
            .paymentMethod(entity.getPaymentMethod())
            .paymentProvider(entity.getPaymentProvider())
            .externalPaymentId(entity.getExternalPaymentId())
            .gatewayTransactionId(entity.getGatewayTransactionId())
            .approvalNumber(entity.getApprovalNumber())
            .failureCode(entity.getFailureCode())
            .failureReason(entity.getFailureReason())
            .cancelReason(entity.getCancelReason())
            .parentPaymentId(entity.getParentPaymentId())
            .refundAmount(entity.getRefundAmount())
            .refundReason(entity.getRefundReason())
            .paymentDetails(entity.getPaymentDetails())
            .authorizedAt(entity.getAuthorizedAt())
            .capturedAt(entity.getCapturedAt())
            .cancelledAt(entity.getCancelledAt())
            .failedAt(entity.getFailedAt())
            .refundedAt(entity.getRefundedAt())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

}

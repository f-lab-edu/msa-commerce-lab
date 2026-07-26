package com.msa.commerce.payment.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.msa.commerce.payment.domain.Payment;

/*
 * payment.result 토픽 계약. order-orchestrator 의 PaymentResultEvent 와 필드가 정확히 일치해야 한다.
 * 소비자의 ObjectMapper 는 FAIL_ON_UNKNOWN_PROPERTIES 기본값(true)을 쓰므로
 * 여기에 필드를 추가하면 소비자가 깨진다.
 */
public record PaymentResultEvent(
    EventMetadata metadata,
    UUID paymentId,
    UUID orderId,
    Long customerId,
    PaymentEventStatus paymentStatus,
    BigDecimal amount,
    String currency,
    String paymentMethod,
    String transactionId,
    LocalDateTime processedAt,
    String failureReason
) {

    public static final String EVENT_TYPE = "PAYMENT_RESULT";

    private static final String SOURCE = "payment-service";

    public static PaymentResultEvent from(Payment payment, String correlationId) {
        return new PaymentResultEvent(
            EventMetadata.create(EVENT_TYPE, SOURCE, correlationId),
            payment.getPaymentId(),
            payment.getOrderId(),
            payment.getCustomerId(),
            PaymentEventStatus.from(payment.getStatus()),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getPaymentMethod().name(),
            payment.getGatewayTransactionId(),
            payment.getUpdatedAt(),
            resolveFailureReason(payment));
    }

    private static String resolveFailureReason(Payment payment) {
        return payment.getFailureReason() != null ? payment.getFailureReason() : payment.getCancelReason();
    }

}

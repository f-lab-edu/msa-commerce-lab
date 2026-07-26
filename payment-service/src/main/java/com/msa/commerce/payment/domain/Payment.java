package com.msa.commerce.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.msa.commerce.common.util.UuidGenerator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "paymentId")
@ToString(exclude = "paymentDetails")
public class Payment {

    private static final String DEFAULT_CURRENCY = "KRW";

    private static final int CURRENCY_LENGTH = 3;

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.9999");

    private Long id;

    private UUID paymentId;

    private UUID orderId;

    private Long customerId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private PaymentMethod paymentMethod;

    private String paymentProvider;

    private String externalPaymentId;

    private String gatewayTransactionId;

    private String approvalNumber;

    private String failureCode;

    private String failureReason;

    private String cancelReason;

    private Long parentPaymentId;

    private BigDecimal refundAmount;

    private String refundReason;

    private Map<String, Object> paymentDetails;

    private LocalDateTime authorizedAt;

    private LocalDateTime capturedAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime failedAt;

    private LocalDateTime refundedAt;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Payment(Long id, UUID paymentId, UUID orderId, Long customerId, BigDecimal amount, String currency,
        PaymentStatus status, PaymentMethod paymentMethod, String paymentProvider, String externalPaymentId,
        String gatewayTransactionId, String approvalNumber, String failureCode, String failureReason,
        String cancelReason, Long parentPaymentId, BigDecimal refundAmount, String refundReason,
        Map<String, Object> paymentDetails,
        LocalDateTime authorizedAt, LocalDateTime capturedAt, LocalDateTime cancelledAt, LocalDateTime failedAt,
        LocalDateTime refundedAt, Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentProvider = paymentProvider;
        this.externalPaymentId = externalPaymentId;
        this.gatewayTransactionId = gatewayTransactionId;
        this.approvalNumber = approvalNumber;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.cancelReason = cancelReason;
        this.parentPaymentId = parentPaymentId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.paymentDetails = paymentDetails != null ? new LinkedHashMap<>(paymentDetails) : new LinkedHashMap<>();
        this.authorizedAt = authorizedAt;
        this.capturedAt = capturedAt;
        this.cancelledAt = cancelledAt;
        this.failedAt = failedAt;
        this.refundedAt = refundedAt;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Payment request(UUID orderId, Long customerId, BigDecimal amount, String currency,
        PaymentMethod paymentMethod, String paymentProvider, Map<String, Object> paymentDetails) {
        validateRequest(orderId, customerId, amount, currency, paymentMethod, paymentProvider);

        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.paymentId = UuidGenerator.generate();
        payment.orderId = orderId;
        payment.customerId = customerId;
        payment.amount = amount;
        payment.currency = currency != null ? currency.toUpperCase() : DEFAULT_CURRENCY;
        payment.status = PaymentStatus.PENDING;
        payment.paymentMethod = paymentMethod;
        payment.paymentProvider = paymentProvider;
        payment.paymentDetails = paymentDetails != null ? new LinkedHashMap<>(paymentDetails) : new LinkedHashMap<>();
        payment.version = 1L;
        payment.createdAt = now;
        payment.updatedAt = now;
        return payment;
    }

    private static void validateRequest(UUID orderId, Long customerId, BigDecimal amount, String currency,
        PaymentMethod paymentMethod, String paymentProvider) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed " + MAX_AMOUNT);
        }
        if (currency != null && currency.length() != CURRENCY_LENGTH) {
            throw new IllegalArgumentException("Currency must be a 3-letter ISO 4217 code");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (paymentProvider == null || paymentProvider.isBlank()) {
            throw new IllegalArgumentException("Payment provider is required");
        }
    }

    public void authorize(String externalPaymentId, String gatewayTransactionId, String approvalNumber) {
        transitionTo(PaymentStatus.AUTHORIZED);

        this.externalPaymentId = externalPaymentId;
        this.gatewayTransactionId = gatewayTransactionId;
        this.approvalNumber = approvalNumber;
        this.authorizedAt = this.updatedAt;
    }

    public void capture(String externalPaymentId, String gatewayTransactionId, String approvalNumber) {
        transitionTo(PaymentStatus.CAPTURED);

        // PENDING -> CAPTURED (즉시 매입) 경로에서는 승인 시각도 함께 기록한다
        if (this.authorizedAt == null) {
            this.authorizedAt = this.updatedAt;
        }
        this.externalPaymentId = externalPaymentId != null ? externalPaymentId : this.externalPaymentId;
        this.gatewayTransactionId = gatewayTransactionId != null ? gatewayTransactionId : this.gatewayTransactionId;
        this.approvalNumber = approvalNumber != null ? approvalNumber : this.approvalNumber;
        this.capturedAt = this.updatedAt;
    }

    public void fail(String failureCode, String failureReason) {
        transitionTo(PaymentStatus.FAILED);

        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.failedAt = this.updatedAt;
    }

    public void cancel(String reason) {
        if (!status.canBeCancelled()) {
            throw new InvalidPaymentStateException(
                "Payment in " + status + " cannot be cancelled; refund it instead");
        }
        transitionTo(PaymentStatus.CANCELLED);

        this.cancelReason = reason;
        this.cancelledAt = this.updatedAt;
    }

    // 부분 환불을 누적한다. 누적액이 결제 금액에 도달하면 전액 환불로 확정된다.
    public void refund(BigDecimal amount, String reason) {
        if (!status.canBeRefunded()) {
            throw new InvalidPaymentStateException("Payment in " + status + " cannot be refunded");
        }
        validateRefundAmount(amount);

        BigDecimal accumulated = refundedSoFar().add(amount);
        transitionTo(accumulated.compareTo(this.amount) == 0
            ? PaymentStatus.REFUNDED
            : PaymentStatus.PARTIAL_REFUNDED);

        this.refundAmount = accumulated;
        this.refundReason = reason;
        this.refundedAt = this.updatedAt;
    }

    public void expire() {
        transitionTo(PaymentStatus.EXPIRED);
    }

    public BigDecimal refundableAmount() {
        return amount.subtract(refundedSoFar());
    }

    public Map<String, Object> getPaymentDetails() {
        return paymentDetails == null ? Collections.emptyMap() : Collections.unmodifiableMap(paymentDetails);
    }

    public boolean isSettled() {
        return status == PaymentStatus.CAPTURED || status == PaymentStatus.PARTIAL_CAPTURED;
    }

    private BigDecimal refundedSoFar() {
        return refundAmount != null ? refundAmount : BigDecimal.ZERO;
    }

    private void validateRefundAmount(BigDecimal requested) {
        if (requested == null || requested.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than 0");
        }
        if (requested.compareTo(refundableAmount()) > 0) {
            throw new IllegalArgumentException(
                "Refund amount exceeds the refundable amount of " + refundableAmount());
        }
    }

    private void transitionTo(PaymentStatus target) {
        if (!status.canTransitionTo(target)) {
            throw InvalidPaymentStateException.transition(status, target);
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

}

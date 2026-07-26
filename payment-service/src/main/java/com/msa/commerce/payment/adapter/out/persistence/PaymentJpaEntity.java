package com.msa.commerce.payment.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "payment_uuid", nullable = false, unique = true, length = 36)
    private UUID paymentId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "order_id", nullable = false, length = 36)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_provider", nullable = false, length = 50)
    private String paymentProvider;

    @Column(name = "external_payment_id", length = 100)
    private String externalPaymentId;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "approval_number", length = 50)
    private String approvalNumber;

    @Column(name = "failure_code", length = 20)
    private String failureCode;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "parent_payment_id")
    private Long parentPaymentId;

    @Column(name = "refund_amount", precision = 12, scale = 4)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_details", columnDefinition = "JSON")
    private Map<String, Object> paymentDetails;

    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Version
    private Long version;

    // 생성/수정 시각은 도메인(Payment)이 상태 전이 시점에 직접 관리한다
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PaymentJpaEntity from(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.id = payment.getId();
        entity.paymentId = payment.getPaymentId();
        entity.orderId = payment.getOrderId();
        entity.customerId = payment.getCustomerId();
        entity.amount = payment.getAmount();
        entity.currency = payment.getCurrency();
        entity.paymentMethod = payment.getPaymentMethod();
        entity.paymentProvider = payment.getPaymentProvider();
        entity.parentPaymentId = payment.getParentPaymentId();
        entity.version = payment.getVersion();
        entity.createdAt = payment.getCreatedAt();
        entity.applyMutableState(payment);
        return entity;
    }

    public void updateFrom(Payment payment) {
        applyMutableState(payment);
    }

    private void applyMutableState(Payment payment) {
        this.status = payment.getStatus();
        this.externalPaymentId = payment.getExternalPaymentId();
        this.gatewayTransactionId = payment.getGatewayTransactionId();
        this.approvalNumber = payment.getApprovalNumber();
        this.failureCode = payment.getFailureCode();
        this.failureReason = payment.getFailureReason();
        this.cancelReason = payment.getCancelReason();
        this.refundAmount = payment.getRefundAmount();
        this.refundReason = payment.getRefundReason();
        this.paymentDetails = new LinkedHashMap<>(payment.getPaymentDetails());
        this.authorizedAt = payment.getAuthorizedAt();
        this.capturedAt = payment.getCapturedAt();
        this.cancelledAt = payment.getCancelledAt();
        this.failedAt = payment.getFailedAt();
        this.refundedAt = payment.getRefundedAt();
        this.updatedAt = payment.getUpdatedAt();
    }

}

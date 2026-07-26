package com.msa.commerce.payment.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@DisplayName("PaymentJpaEntity / PaymentDomainMapper 변환 테스트")
class PaymentJpaEntityTest {

    private static final String CARD_LAST4_KEY = "cardLast4";

    private static final String CARD_LAST4 = "1234";

    private final PaymentDomainMapper mapper = new PaymentDomainMapper();

    @Test
    @DisplayName("도메인 → 엔티티 → 도메인 왕복 변환에서 값이 보존된다")
    void roundTripConversion() {
        Payment payment = capturedPayment();

        Payment restored = mapper.toDomain(PaymentJpaEntity.from(payment));

        assertThat(restored.getPaymentId()).isEqualTo(payment.getPaymentId());
        assertThat(restored.getOrderId()).isEqualTo(payment.getOrderId());
        assertThat(restored.getCustomerId()).isEqualTo(payment.getCustomerId());
        assertThat(restored.getAmount()).isEqualByComparingTo(payment.getAmount());
        assertThat(restored.getCurrency()).isEqualTo(payment.getCurrency());
        assertThat(restored.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(restored.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(restored.getPaymentProvider()).isEqualTo(payment.getPaymentProvider());
        assertThat(restored.getGatewayTransactionId()).isEqualTo("TXN-1");
        assertThat(restored.getApprovalNumber()).isEqualTo("0001");
        assertThat(restored.getPaymentDetails()).containsEntry(CARD_LAST4_KEY, CARD_LAST4);
        assertThat(restored.getAuthorizedAt()).isEqualTo(payment.getAuthorizedAt());
        assertThat(restored.getCapturedAt()).isEqualTo(payment.getCapturedAt());
        assertThat(restored.getCreatedAt()).isEqualTo(payment.getCreatedAt());
    }

    @Test
    @DisplayName("updateFrom 은 변경 가능한 상태만 갱신하고 식별자는 건드리지 않는다")
    void updateFromOnlyTouchesMutableState() {
        Payment pending = pendingPayment();
        PaymentJpaEntity entity = PaymentJpaEntity.from(pending);
        UUID originalPaymentId = entity.getPaymentId();
        var originalCreatedAt = entity.getCreatedAt();

        pending.fail("LIMIT_EXCEEDED", "한도 초과");
        entity.updateFrom(pending);

        assertThat(entity.getPaymentId()).isEqualTo(originalPaymentId);
        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(entity.getFailureCode()).isEqualTo("LIMIT_EXCEEDED");
        assertThat(entity.getFailedAt()).isNotNull();
    }

    @Test
    @DisplayName("엔티티의 paymentDetails 는 도메인과 분리된 복사본이다")
    void copiesPaymentDetails() {
        Payment payment = pendingPayment();
        PaymentJpaEntity entity = PaymentJpaEntity.from(payment);

        entity.getPaymentDetails().put(CARD_LAST4_KEY, "9999");

        assertThat(payment.getPaymentDetails()).containsEntry(CARD_LAST4_KEY, CARD_LAST4);
    }

    @Test
    @DisplayName("null 엔티티는 null 도메인으로 변환된다")
    void mapsNullToNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    private Payment pendingPayment() {
        return Payment.request(UUID.randomUUID(), 1001L, new BigDecimal("15000.0000"), "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", Map.of(CARD_LAST4_KEY, CARD_LAST4));
    }

    private Payment capturedPayment() {
        Payment payment = pendingPayment();
        payment.capture("EXT-1", "TXN-1", "0001");
        return payment;
    }

}

package com.msa.commerce.payment.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("PaymentStatus 상태 전이 규칙 테스트")
class PaymentStatusTest {

    @ParameterizedTest(name = "{0} -> {1} 은 허용된다")
    @CsvSource({
        "PENDING, AUTHORIZED",
        "PENDING, CAPTURED",
        "PENDING, FAILED",
        "PENDING, CANCELLED",
        "PENDING, EXPIRED",
        "AUTHORIZED, CAPTURED",
        "AUTHORIZED, PARTIAL_CAPTURED",
        "AUTHORIZED, CANCELLED",
        "CAPTURED, REFUNDED",
        "CAPTURED, PARTIAL_REFUNDED",
        "PARTIAL_CAPTURED, CAPTURED",
        "PARTIAL_REFUNDED, REFUNDED"
    })
    void allowedTransitions(PaymentStatus from, PaymentStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} 은 거부된다")
    @CsvSource({
        "PENDING, REFUNDED",
        "PENDING, PARTIAL_REFUNDED",
        "AUTHORIZED, REFUNDED",
        "CAPTURED, CANCELLED",
        "CAPTURED, AUTHORIZED",
        "FAILED, CAPTURED",
        "CANCELLED, CAPTURED",
        "REFUNDED, CAPTURED",
        "EXPIRED, AUTHORIZED"
    })
    void rejectedTransitions(PaymentStatus from, PaymentStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @Test
    @DisplayName("null 로의 전이는 항상 거부된다")
    void transitionToNullIsRejected() {
        assertThat(PaymentStatus.PENDING.canTransitionTo(null)).isFalse();
    }

    @ParameterizedTest(name = "{0} 은 종료 상태다")
    @EnumSource(value = PaymentStatus.class, names = {"CANCELLED", "FAILED", "REFUNDED", "EXPIRED"})
    void terminalStatuses(PaymentStatus status) {
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.isActive()).isFalse();
    }

    @ParameterizedTest(name = "{0} 은 활성 상태다")
    @EnumSource(value = PaymentStatus.class, names = {"PENDING", "AUTHORIZED", "CAPTURED", "PARTIAL_CAPTURED"})
    void activeStatuses(PaymentStatus status) {
        assertThat(status.isActive()).isTrue();
        assertThat(status.isTerminal()).isFalse();
    }

    @Test
    @DisplayName("취소는 아직 매입되지 않은 결제만 가능하다")
    void cancellableStatuses() {
        assertThat(PaymentStatus.PENDING.canBeCancelled()).isTrue();
        assertThat(PaymentStatus.AUTHORIZED.canBeCancelled()).isTrue();
        assertThat(PaymentStatus.CAPTURED.canBeCancelled()).isFalse();
    }

    @Test
    @DisplayName("환불은 매입된 결제만 가능하다")
    void refundableStatuses() {
        assertThat(PaymentStatus.CAPTURED.canBeRefunded()).isTrue();
        assertThat(PaymentStatus.PARTIAL_CAPTURED.canBeRefunded()).isTrue();
        assertThat(PaymentStatus.PARTIAL_REFUNDED.canBeRefunded()).isTrue();
        assertThat(PaymentStatus.PENDING.canBeRefunded()).isFalse();
        assertThat(PaymentStatus.AUTHORIZED.canBeRefunded()).isFalse();
    }

    @Test
    @DisplayName("activeStatuses() 는 활성 상태 4종을 반환한다")
    void activeStatusesSet() {
        assertThat(PaymentStatus.activeStatuses()).containsExactlyInAnyOrder(
            PaymentStatus.PENDING,
            PaymentStatus.AUTHORIZED,
            PaymentStatus.CAPTURED,
            PaymentStatus.PARTIAL_CAPTURED);
    }

}

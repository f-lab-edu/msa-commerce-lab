package com.msa.commerce.payment.domain;

import java.util.EnumSet;
import java.util.Set;

public enum PaymentStatus {

    PENDING,

    AUTHORIZED,

    CAPTURED,

    PARTIAL_CAPTURED,

    CANCELLED,

    FAILED,

    REFUNDED,

    PARTIAL_REFUNDED,

    EXPIRED;

    // 주문당 중복 결제를 막기 위한 판단 기준: 아직 살아 있는 결제인가
    public static Set<PaymentStatus> activeStatuses() {
        return EnumSet.of(PENDING, AUTHORIZED, CAPTURED, PARTIAL_CAPTURED);
    }

    public Set<PaymentStatus> allowedTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(AUTHORIZED, CAPTURED, CANCELLED, FAILED, EXPIRED);
            case AUTHORIZED -> EnumSet.of(CAPTURED, PARTIAL_CAPTURED, CANCELLED, FAILED, EXPIRED);
            case PARTIAL_CAPTURED -> EnumSet.of(CAPTURED, PARTIAL_CAPTURED, PARTIAL_REFUNDED, REFUNDED, CANCELLED);
            case CAPTURED -> EnumSet.of(PARTIAL_REFUNDED, REFUNDED);
            case PARTIAL_REFUNDED -> EnumSet.of(PARTIAL_REFUNDED, REFUNDED);
            case CANCELLED, FAILED, REFUNDED, EXPIRED -> EnumSet.noneOf(PaymentStatus.class);
        };
    }

    public boolean canTransitionTo(PaymentStatus target) {
        return target != null && allowedTransitions().contains(target);
    }

    public boolean isTerminal() {
        return allowedTransitions().isEmpty();
    }

    public boolean isActive() {
        return activeStatuses().contains(this);
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == AUTHORIZED;
    }

    public boolean canBeRefunded() {
        return this == CAPTURED || this == PARTIAL_CAPTURED || this == PARTIAL_REFUNDED;
    }

}

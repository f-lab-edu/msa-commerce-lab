package com.msa.commerce.payment.domain.event;

import com.msa.commerce.payment.domain.PaymentStatus;

/*
 * 내부 상태 9종을 order-orchestrator 가 이해하는 4종으로 좁힌다.
 * 이름과 상수는 orchestrator 의 PaymentResultEvent.PaymentStatus 와 같아야 한다.
 */
public enum PaymentEventStatus {

    SUCCESS,

    FAILED,

    PENDING,

    CANCELLED;

    public static PaymentEventStatus from(PaymentStatus status) {
        return switch (status) {
            case CAPTURED, PARTIAL_CAPTURED, PARTIAL_REFUNDED -> SUCCESS;
            case PENDING, AUTHORIZED -> PENDING;
            case FAILED, EXPIRED -> FAILED;
            case CANCELLED, REFUNDED -> CANCELLED;
        };
    }

}

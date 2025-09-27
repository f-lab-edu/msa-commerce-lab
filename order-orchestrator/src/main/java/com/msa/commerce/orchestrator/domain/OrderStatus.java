package com.msa.commerce.orchestrator.domain;

public enum OrderStatus {

    PENDING,

    CONFIRMED,

    PAYMENT_PENDING,

    PAID,

    PROCESSING,

    SHIPPED,

    DELIVERED,

    CANCELLED,

    REFUNDED,

    FAILED;

    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED || this == PAYMENT_PENDING;
    }

    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED || this == FAILED;
    }

    public boolean isProcessing() {
        return this == PAID || this == PROCESSING || this == SHIPPED;
    }

}

package com.msa.commerce.orchestrator.domain;

/**
 * Order status enumeration representing the lifecycle of an order.
 * Maps to the database enum values defined in the migration.
 */
public enum OrderStatus {
    /**
     * Order has been created but not yet confirmed
     */
    PENDING,

    /**
     * Order has been confirmed by the customer
     */
    CONFIRMED,

    /**
     * Order payment is pending
     */
    PAYMENT_PENDING,

    /**
     * Order payment has been completed
     */
    PAID,

    /**
     * Order is being prepared for shipment
     */
    PROCESSING,

    /**
     * Order has been shipped to customer
     */
    SHIPPED,

    /**
     * Order has been delivered to customer
     */
    DELIVERED,

    /**
     * Order has been cancelled
     */
    CANCELLED,

    /**
     * Order payment has been refunded
     */
    REFUNDED,

    /**
     * Order processing has failed
     */
    FAILED;

    /**
     * Checks if the order status allows cancellation
     * @return true if order can be cancelled, false otherwise
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED || this == PAYMENT_PENDING;
    }

    /**
     * Checks if the order status represents a completed state
     * @return true if order is in completed state, false otherwise
     */
    public boolean isCompleted() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED || this == FAILED;
    }

    /**
     * Checks if the order status represents an active processing state
     * @return true if order is being actively processed, false otherwise
     */
    public boolean isProcessing() {
        return this == PAID || this == PROCESSING || this == SHIPPED;
    }
}
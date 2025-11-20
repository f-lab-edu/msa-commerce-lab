package com.msa.commerce.orchestrator.domain.event;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EventValidator {

    public static void validate(DomainEvent event) {
        List<String> errors = new ArrayList<>();

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (event.getMetadata() == null) {
            errors.add("Event metadata cannot be null");
        } else {
            validateMetadata(event.getMetadata(), errors);
        }

        if (event.getAggregateId() == null || event.getAggregateId().trim().isEmpty()) {
            errors.add("Aggregate ID cannot be null or empty");
        }

        switch (event) {
            case OrderCreatedEvent orderCreatedEvent -> validateOrderCreatedEvent(orderCreatedEvent, errors);
            case OrderUpdatedEvent orderUpdatedEvent -> validateOrderUpdatedEvent(orderUpdatedEvent, errors);
            case PaymentResultEvent paymentResultEvent -> validatePaymentResultEvent(paymentResultEvent, errors);
            default -> {
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                "Event validation failed: " + String.join(", ", errors)
            );
        }
    }

    private static void validateMetadata(EventMetadata metadata, List<String> errors) {
        if (metadata.getEventId() == null || metadata.getEventId().trim().isEmpty()) {
            errors.add("Event ID cannot be null or empty");
        }

        if (metadata.getCorrelationId() == null || metadata.getCorrelationId().trim().isEmpty()) {
            errors.add("Correlation ID cannot be null or empty");
        }

        if (metadata.getTimestamp() == null) {
            errors.add("Timestamp cannot be null");
        }

        if (metadata.getEventType() == null || metadata.getEventType().trim().isEmpty()) {
            errors.add("Event type cannot be null or empty");
        }

        if (metadata.getSource() == null || metadata.getSource().trim().isEmpty()) {
            errors.add("Source cannot be null or empty");
        }

        if (metadata.getVersion() == null || metadata.getVersion() <= 0) {
            errors.add("Version must be greater than 0");
        }
    }

    private static void validateOrderCreatedEvent(OrderCreatedEvent event, List<String> errors) {
        if (event.getOrderId() == null) {
            errors.add("Order ID cannot be null");
        }

        if (event.getOrderNumber() == null || event.getOrderNumber().trim().isEmpty()) {
            errors.add("Order number cannot be null or empty");
        }

        if (event.getCustomerId() == null) {
            errors.add("Customer ID cannot be null");
        }

        if (event.getTotalAmount() == null) {
            errors.add("Total amount cannot be null");
        }

        if (event.getCurrency() == null || event.getCurrency().trim().isEmpty()) {
            errors.add("Currency cannot be null or empty");
        }

        if (event.getOrderItems() == null || event.getOrderItems().isEmpty()) {
            errors.add("Order items cannot be null or empty");
        }

        if (event.getOrderDate() == null) {
            errors.add("Order date cannot be null");
        }
    }

    private static void validateOrderUpdatedEvent(OrderUpdatedEvent event, List<String> errors) {
        if (event.getOrderId() == null) {
            errors.add("Order ID cannot be null");
        }

        if (event.getOrderNumber() == null || event.getOrderNumber().trim().isEmpty()) {
            errors.add("Order number cannot be null or empty");
        }

        if (event.getPreviousStatus() == null) {
            errors.add("Previous status cannot be null");
        }

        if (event.getCurrentStatus() == null) {
            errors.add("Current status cannot be null");
        }

        if (event.getStatusChangedAt() == null) {
            errors.add("Status changed at cannot be null");
        }
    }

    private static void validatePaymentResultEvent(PaymentResultEvent event, List<String> errors) {
        if (event.getPaymentId() == null) {
            errors.add("Payment ID cannot be null");
        }

        if (event.getOrderId() == null) {
            errors.add("Order ID cannot be null");
        }

        if (event.getCustomerId() == null) {
            errors.add("Customer ID cannot be null");
        }

        if (event.getPaymentStatus() == null) {
            errors.add("Payment status cannot be null");
        }

        if (event.getAmount() == null) {
            errors.add("Amount cannot be null");
        }

        if (event.getCurrency() == null || event.getCurrency().trim().isEmpty()) {
            errors.add("Currency cannot be null or empty");
        }

        if (event.getProcessedAt() == null) {
            errors.add("Processed at cannot be null");
        }
    }

}

package com.msa.commerce.orchestrator.domain.event;

import java.util.stream.Collectors;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderEventMapper {

    private static final String EVENT_SOURCE = "order-orchestrator";

    public static OrderCreatedEvent toOrderCreatedEvent(Order order) {
        return toOrderCreatedEvent(order, null);
    }

    public static OrderCreatedEvent toOrderCreatedEvent(Order order, String correlationId) {
        EventMetadata metadata = correlationId != null
            ? EventMetadata.create("OrderCreated", EVENT_SOURCE, correlationId)
            : EventMetadata.create("OrderCreated", EVENT_SOURCE);

        return OrderCreatedEvent.builder()
            .metadata(metadata)
            .orderId(order.getOrderId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomerId())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .shippingAddress(order.getShippingAddress())
            .orderItems(order.getOrderItems().stream()
                .map(OrderEventMapper::toOrderItemData)
                .collect(Collectors.toList()))
            .sourceChannel(order.getSourceChannel())
            .orderDate(order.getOrderDate())
            .build();
    }

    public static OrderUpdatedEvent toOrderUpdatedEvent(
        Order order,
        OrderStatus previousStatus,
        String reason
    ) {
        return toOrderUpdatedEvent(order, previousStatus, reason, null);
    }

    public static OrderUpdatedEvent toOrderUpdatedEvent(
        Order order,
        OrderStatus previousStatus,
        String reason,
        String correlationId
    ) {
        EventMetadata metadata = correlationId != null
            ? EventMetadata.create("OrderUpdated", EVENT_SOURCE, correlationId)
            : EventMetadata.create("OrderUpdated", EVENT_SOURCE);

        return OrderUpdatedEvent.builder()
            .metadata(metadata)
            .orderId(order.getOrderId())
            .orderNumber(order.getOrderNumber())
            .previousStatus(previousStatus)
            .currentStatus(order.getStatus())
            .customerId(order.getCustomerId())
            .statusChangedAt(order.getUpdatedAt())
            .reason(reason)
            .build();
    }

    private static OrderCreatedEvent.OrderItemData toOrderItemData(OrderItem orderItem) {
        return OrderCreatedEvent.OrderItemData.builder()
            .productId(orderItem.getProductId())
            .productName(orderItem.getProductName())
            .quantity(orderItem.getQuantity())
            .unitPrice(orderItem.getUnitPrice())
            .totalPrice(orderItem.getTotalPrice())
            .build();
    }

}

package com.msa.commerce.orchestrator.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.application.port.in.OrderItemResponse;
import com.msa.commerce.orchestrator.application.port.in.OrderResponse;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
            .orderId(order.getOrderId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomerId())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .orderDate(order.getOrderDate())
            .orderItems(toOrderItemResponseList(order.getOrderItems()))
            .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        return OrderItemResponse.builder()
            .orderItemId(orderItem.getOrderItemId())
            .productId(orderItem.getProductId())
            .productName(orderItem.getProductName())
            .productSku(orderItem.getProductSku())
            .productVariantId(orderItem.getProductVariantId())
            .variantName(orderItem.getVariantName())
            .quantity(orderItem.getQuantity())
            .unitPrice(orderItem.getUnitPrice())
            .totalPrice(orderItem.getTotalPrice())
            .build();
    }

    private List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return null;
        }

        return orderItems.stream()
            .map(this::toOrderItemResponse)
            .collect(Collectors.toList());
    }
}

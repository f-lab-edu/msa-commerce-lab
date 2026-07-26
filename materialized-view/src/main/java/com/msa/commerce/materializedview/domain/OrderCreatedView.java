package com.msa.commerce.materializedview.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedView(
    String eventId,
    UUID orderId,
    Long customerId,
    BigDecimal totalAmount,
    LocalDateTime orderDate,
    List<OrderItemView> orderItems
) {

    public OrderCreatedView {
        orderItems = orderItems != null ? List.copyOf(orderItems) : List.of();
    }

    public int totalQuantity() {
        return orderItems.stream()
            .mapToInt(OrderItemView::quantity)
            .sum();
    }

    public record OrderItemView(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal totalPrice
    ) {

    }

}

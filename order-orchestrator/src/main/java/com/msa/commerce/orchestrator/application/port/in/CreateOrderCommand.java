package com.msa.commerce.orchestrator.application.port.in;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;

@Builder
public record CreateOrderCommand(
    Long customerId,
    List<OrderItemCommand> orderItems
) {

    @Builder
    public record OrderItemCommand(
        Long productId,
        Integer quantity,
        BigDecimal unitPrice
    ) {
    }
}

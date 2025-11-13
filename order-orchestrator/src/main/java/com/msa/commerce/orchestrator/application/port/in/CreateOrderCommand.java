package com.msa.commerce.orchestrator.application.port.in;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record CreateOrderCommand(
    String orderNumber,
    Long customerId,
    Map<String, Object> shippingAddress,
    String sourceChannel,
    List<OrderItemCommand> orderItems
) {

    @Builder
    public record OrderItemCommand(
        Long productId,
        String productName,
        String productSku,
        Long productVariantId,
        String variantName,
        Integer quantity,
        java.math.BigDecimal unitPrice
    ) {

    }

}

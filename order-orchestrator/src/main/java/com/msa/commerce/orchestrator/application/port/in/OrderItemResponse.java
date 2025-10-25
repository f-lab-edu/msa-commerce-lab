package com.msa.commerce.orchestrator.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;

@Builder
public record OrderItemResponse(
    UUID orderItemId,
    Long productId,
    String productName,
    String productSku,
    Long productVariantId,
    String variantName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {
}

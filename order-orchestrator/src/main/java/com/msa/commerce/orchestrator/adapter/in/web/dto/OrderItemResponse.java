package com.msa.commerce.orchestrator.adapter.in.web.dto;

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

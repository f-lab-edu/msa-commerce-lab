package com.msa.commerce.orchestrator.adapter.out.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;

@Builder
public record OrderItemEvent(
    UUID orderItemId,
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {

}

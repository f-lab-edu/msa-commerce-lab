package com.msa.commerce.orchestrator.adapter.in.web.dto.response;

import java.util.UUID;

import lombok.Builder;

@Builder
public record CreateOrderResponse(
    UUID orderId
) {

}

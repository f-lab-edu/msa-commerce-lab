package com.msa.commerce.orchestrator.adapter.in.web.dto.response;

import lombok.Builder;

@Builder
public record UpdateOrderStatusResponse(
    String message
) {

}

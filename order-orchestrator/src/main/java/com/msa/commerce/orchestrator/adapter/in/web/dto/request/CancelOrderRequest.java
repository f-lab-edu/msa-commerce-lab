package com.msa.commerce.orchestrator.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelOrderRequest(@NotBlank(message = "Cancellation reason is required") String reason) {

}

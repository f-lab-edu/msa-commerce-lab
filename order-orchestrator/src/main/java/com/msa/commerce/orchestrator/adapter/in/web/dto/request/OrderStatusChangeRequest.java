package com.msa.commerce.orchestrator.adapter.in.web.dto.request;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderStatusChangeRequest(

    @NotNull(message = "New status is required")
    OrderStatus newStatus,

    @NotBlank(message = "Reason is required")
    String reason

) {

}

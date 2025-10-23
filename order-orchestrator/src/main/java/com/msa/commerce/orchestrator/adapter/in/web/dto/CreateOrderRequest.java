package com.msa.commerce.orchestrator.adapter.in.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateOrderRequest(

    @NotNull(message = "Customer ID is required")
    Long customerId,

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    List<OrderItemRequest> orderItems
) {
}

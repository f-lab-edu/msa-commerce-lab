package com.msa.commerce.orchestrator.adapter.in.web.dto.request;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;

    private String reason;

}

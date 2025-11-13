package com.msa.commerce.orchestrator.adapter.in.web.dto.request;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateOrderStatusRequest(
    @NotNull(message = "변경할 주문 상태는 필수입니다.")
    OrderStatus newStatus
) {

}

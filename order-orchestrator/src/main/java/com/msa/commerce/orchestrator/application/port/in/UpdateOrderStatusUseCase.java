package com.msa.commerce.orchestrator.application.port.in;

import java.util.UUID;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface UpdateOrderStatusUseCase {

    void updateOrderStatus(UUID orderId, OrderStatus newStatus, String reason);

}

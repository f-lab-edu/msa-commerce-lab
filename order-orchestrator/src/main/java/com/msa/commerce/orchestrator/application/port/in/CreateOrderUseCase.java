package com.msa.commerce.orchestrator.application.port.in;

import com.msa.commerce.orchestrator.domain.Order;

public interface CreateOrderUseCase {

    Order createOrder(CreateOrderCommand command);
}

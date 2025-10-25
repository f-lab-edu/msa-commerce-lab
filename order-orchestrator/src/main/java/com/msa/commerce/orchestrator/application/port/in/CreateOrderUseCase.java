package com.msa.commerce.orchestrator.application.port.in;

public interface CreateOrderUseCase {

    OrderResponse createOrder(CreateOrderCommand command);

}

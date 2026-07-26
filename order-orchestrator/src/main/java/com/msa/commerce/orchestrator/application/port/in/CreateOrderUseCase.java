package com.msa.commerce.orchestrator.application.port.in;

import java.util.UUID;

public interface CreateOrderUseCase {

    UUID createOrder(CreateOrderCommand command);

}

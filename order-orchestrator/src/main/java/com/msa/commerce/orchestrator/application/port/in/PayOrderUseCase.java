package com.msa.commerce.orchestrator.application.port.in;

import com.msa.commerce.orchestrator.application.port.in.command.PayOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;

public interface PayOrderUseCase {

    OrderResponse pay(PayOrderCommand command);

}

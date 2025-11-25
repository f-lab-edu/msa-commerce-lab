package com.msa.commerce.orchestrator.application.port.in;

import com.msa.commerce.orchestrator.application.port.in.command.CancelOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;

public interface CancelOrderUseCase {

    OrderResponse cancel(CancelOrderCommand command);

}

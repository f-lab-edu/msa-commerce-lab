package com.msa.commerce.orchestrator.application.port.in;

import com.msa.commerce.orchestrator.application.port.in.command.ConfirmOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;

public interface ConfirmOrderUseCase {

    OrderResponse confirm(ConfirmOrderCommand command);

}

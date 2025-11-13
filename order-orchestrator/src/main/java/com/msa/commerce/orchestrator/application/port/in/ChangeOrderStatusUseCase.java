package com.msa.commerce.orchestrator.application.port.in;

import com.msa.commerce.orchestrator.application.port.in.command.ChangeOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderStatusChangeResult;

public interface ChangeOrderStatusUseCase {

    OrderStatusChangeResult changeOrderStatus(ChangeOrderStatusCommand command);

}

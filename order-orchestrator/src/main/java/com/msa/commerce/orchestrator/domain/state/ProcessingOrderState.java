package com.msa.commerce.orchestrator.domain.state;

import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public class ProcessingOrderState extends AbstractOrderState {

    public ProcessingOrderState() {
        super(
            OrderStatus.PROCESSING,
            Set.of(OrderStatus.SHIPPED),
            false
        );
    }

    @Override
    public void updateTimestamp(Order order) {
    }

}

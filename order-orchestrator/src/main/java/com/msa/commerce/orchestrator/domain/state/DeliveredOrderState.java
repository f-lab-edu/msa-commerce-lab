package com.msa.commerce.orchestrator.domain.state;

import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public class DeliveredOrderState extends AbstractOrderState {

    public DeliveredOrderState() {
        super(
            OrderStatus.DELIVERED,
            Set.of(),
            false
        );
    }

    @Override
    public void updateTimestamp(Order order) {
        order.recordDeliveryTimestamp(now());
    }

}

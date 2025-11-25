package com.msa.commerce.orchestrator.domain.state;

import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public class ShippedOrderState extends AbstractOrderState {

    public ShippedOrderState() {
        super(
            OrderStatus.SHIPPED,
            Set.of(OrderStatus.DELIVERED),
            false
        );
    }

    @Override
    public void updateTimestamp(Order order) {
        order.recordShipmentTimestamp(now());
    }

}

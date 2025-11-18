package com.msa.commerce.orchestrator.domain.state;

import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public class PendingOrderState extends AbstractOrderState {

    public PendingOrderState() {
        super(
            OrderStatus.PENDING,
            Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            true
        );
    }

    @Override
    protected void performAdditionalValidation(Order order, OrderStatus targetStatus) {
        if (targetStatus == OrderStatus.CONFIRMED && order.getOrderItems().isEmpty()) {
            throw new IllegalStateException("Cannot confirm order with no items");
        }
    }

    @Override
    public void updateTimestamp(Order order) {
    }

}

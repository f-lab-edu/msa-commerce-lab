package com.msa.commerce.orchestrator.domain.state;

import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public class ConfirmedOrderState extends AbstractOrderState {

    public ConfirmedOrderState() {
        super(
            OrderStatus.CONFIRMED,
            Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),
            true
        );
    }

    @Override
    public void updateTimestamp(Order order) {
        order.recordConfirmationTimestamp(now());
    }

}

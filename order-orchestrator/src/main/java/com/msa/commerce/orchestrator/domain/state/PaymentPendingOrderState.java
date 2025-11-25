package com.msa.commerce.orchestrator.domain.state;

import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public class PaymentPendingOrderState extends AbstractOrderState {

    public PaymentPendingOrderState() {
        super(
            OrderStatus.PAYMENT_PENDING,
            Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            true
        );
    }

    @Override
    public void updateTimestamp(Order order) {
    }

}

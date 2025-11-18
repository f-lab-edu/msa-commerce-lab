package com.msa.commerce.orchestrator.domain.state;

import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public class PaidOrderState extends AbstractOrderState {

    public PaidOrderState() {
        super(
            OrderStatus.PAID,
            Set.of(OrderStatus.PROCESSING),
            false
        );
    }

    @Override
    public void updateTimestamp(Order order) {
        order.recordPaymentCompletionTimestamp(now());
    }

}

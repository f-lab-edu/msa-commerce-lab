package com.msa.commerce.orchestrator.domain.state;

import java.time.LocalDateTime;
import java.util.Set;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public abstract class AbstractOrderState implements OrderState {

    protected final OrderStatus status;

    protected final Set<OrderStatus> allowedTransitions;

    protected final boolean canModifyItems;

    protected AbstractOrderState(OrderStatus status, Set<OrderStatus> allowedTransitions, boolean canModifyItems) {
        this.status = status;
        this.allowedTransitions = allowedTransitions;
        this.canModifyItems = canModifyItems;
    }

    @Override
    public boolean canTransitionTo(OrderStatus targetStatus) {
        return allowedTransitions.contains(targetStatus);
    }

    @Override
    public void validateTransition(Order order, OrderStatus targetStatus) {
        if (!canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", status, targetStatus)
            );
        }
        performAdditionalValidation(order, targetStatus);
    }

    protected void performAdditionalValidation(Order order, OrderStatus targetStatus) {
    }

    @Override
    public void validateAddItem(Order order) {
        if (!canModifyItems) {
            throw new IllegalStateException("Cannot add items to order in status: " + status);
        }
    }

    @Override
    public void validateRemoveItem(Order order) {
        if (!canModifyItems) {
            throw new IllegalStateException("Cannot remove items from order in status: " + status);
        }
    }

    @Override
    public OrderStatus getStatus() {
        return status;
    }

    protected LocalDateTime now() {
        return LocalDateTime.now();
    }

}

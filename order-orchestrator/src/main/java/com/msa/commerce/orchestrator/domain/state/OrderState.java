package com.msa.commerce.orchestrator.domain.state;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface OrderState {

    boolean canTransitionTo(OrderStatus targetStatus);

    void validateTransition(Order order, OrderStatus targetStatus);

    void updateTimestamp(Order order);

    void validateAddItem(Order order);

    void validateRemoveItem(Order order);

    OrderStatus getStatus();

}

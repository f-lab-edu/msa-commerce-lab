package com.msa.commerce.orchestrator.application.port.out;

import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

public interface OrderEventPublisher {

    void publishOrderCreated(Order order);

    void publishOrderCreated(Order order, String correlationId);

    void publishOrderUpdated(Order order, OrderStatus previousStatus, String reason);

    void publishOrderUpdated(Order order, OrderStatus previousStatus, String reason, String correlationId);

}

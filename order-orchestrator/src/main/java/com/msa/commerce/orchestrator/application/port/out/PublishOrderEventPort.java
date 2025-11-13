package com.msa.commerce.orchestrator.application.port.out;

import com.msa.commerce.orchestrator.domain.Order;

public interface PublishOrderEventPort {

    void publishOrderCreatedEvent(Order order);

    void publishOrderStatusChangedEvent(Order order);
}

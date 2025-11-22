package com.msa.commerce.orchestrator.application.port.out;

import com.msa.commerce.orchestrator.domain.event.DomainEvent;

public interface EventPublisher {

    void publish(String topic, DomainEvent event);

    void publish(String topic, String key, DomainEvent event);

}

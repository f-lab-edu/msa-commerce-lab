package com.msa.commerce.orchestrator.application.port.out;

import java.util.List;

import com.msa.commerce.orchestrator.domain.crossdomain.CrossDomainEvent;
import com.msa.commerce.orchestrator.domain.crossdomain.PublishingStatus;

public interface CrossDomainEventRepository {
 
    CrossDomainEvent save(CrossDomainEvent event);

    List<CrossDomainEvent> findPendingEvents(int limit);

    long countByStatus(PublishingStatus status);

}

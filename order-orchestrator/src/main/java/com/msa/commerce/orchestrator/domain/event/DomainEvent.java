package com.msa.commerce.orchestrator.domain.event;

import java.io.Serializable;

public interface DomainEvent extends Serializable {

    EventMetadata getMetadata();

    String getAggregateId();

}

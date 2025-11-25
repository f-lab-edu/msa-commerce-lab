package com.msa.commerce.orchestrator.application.port.out;

import java.util.List;

import com.msa.commerce.orchestrator.domain.outbox.OutboxEvent;
import com.msa.commerce.orchestrator.domain.outbox.OutboxStatus;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findPendingEvents(int limit);

    long countByStatus(OutboxStatus status);

}

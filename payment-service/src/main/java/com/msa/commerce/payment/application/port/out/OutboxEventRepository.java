package com.msa.commerce.payment.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msa.commerce.payment.domain.outbox.OutboxEvent;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findPending(int limit);

    Optional<OutboxEvent> findByEventId(String eventId);

}

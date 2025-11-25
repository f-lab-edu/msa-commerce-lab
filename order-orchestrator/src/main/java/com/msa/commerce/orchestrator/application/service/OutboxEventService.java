package com.msa.commerce.orchestrator.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.orchestrator.application.port.out.OutboxEventRepository;
import com.msa.commerce.orchestrator.domain.event.DomainEvent;
import com.msa.commerce.orchestrator.domain.outbox.OutboxEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional
    public void saveEvent(String aggregateType, String aggregateId, String eventType,
        String topic, DomainEvent event, String correlationId) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.create(
                aggregateType,
                aggregateId,
                eventType,
                topic,
                payload,
                correlationId
            );

            outboxEventRepository.save(outboxEvent);

            log.info("Saved event to outbox: aggregateId={}, eventType={}, eventId={}", aggregateId, eventType, event.getMetadata().getEventId());

        } catch (JsonProcessingException e) {
            throw new OutboxEventSerializationException("Failed to serialize domain event: " + eventType, e);
        }
    }

    public static class OutboxEventSerializationException extends RuntimeException {

        public OutboxEventSerializationException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}

package com.msa.commerce.orchestrator.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.orchestrator.application.port.out.CrossDomainEventRepository;
import com.msa.commerce.orchestrator.domain.crossdomain.CrossDomainEvent;
import com.msa.commerce.orchestrator.domain.crossdomain.DomainType;
import com.msa.commerce.orchestrator.domain.crossdomain.EntityType;
import com.msa.commerce.orchestrator.domain.crossdomain.EventType;
import com.msa.commerce.orchestrator.domain.event.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossDomainEventService {

    private final CrossDomainEventRepository crossDomainEventRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(EventType eventType, DomainType sourceDomain, List<DomainType> targetDomains,
        EntityType entityType, String entityId, String entityUuid,
        DomainEvent event, String correlationId, String kafkaTopic) {

        try {
            String eventData = objectMapper.writeValueAsString(event);

            CrossDomainEvent crossDomainEvent = CrossDomainEvent.create(
                eventType,
                sourceDomain,
                targetDomains,
                entityType,
                entityId,
                entityUuid,
                eventData,
                correlationId,
                kafkaTopic
            );

            crossDomainEventRepository.save(crossDomainEvent);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event data", e);
        }
    }

}

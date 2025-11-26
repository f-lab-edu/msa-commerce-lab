package com.msa.commerce.orchestrator.adapter.out.crossdomain;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.common.exception.kafka.EventPublishException;
import com.msa.commerce.orchestrator.application.port.out.CrossDomainEventRepository;
import com.msa.commerce.orchestrator.domain.crossdomain.CrossDomainEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CrossDomainEventRelay {

    private final CrossDomainEventRepository crossDomainEventRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final int batchSize;

    private final int publishTimeoutSeconds;

    public CrossDomainEventRelay(
        CrossDomainEventRepository crossDomainEventRepository,
        KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${app.kafka.relay.batch-size:100}") int batchSize,
        @Value("${app.kafka.relay.publish-timeout-seconds:5}") int publishTimeoutSeconds
    ) {
        this.crossDomainEventRepository = crossDomainEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.batchSize = batchSize;
        this.publishTimeoutSeconds = publishTimeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${app.kafka.relay.fixed-delay-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        List<CrossDomainEvent> pendingEvents = crossDomainEventRepository.findPendingEvents(batchSize);

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (CrossDomainEvent event : pendingEvents) {
            try {
                SendResult<String, Object> result = publishEvent(event);
                int partition = result.getRecordMetadata().partition();
                long offset = result.getRecordMetadata().offset();

                event.markAsPublished(partition, offset);
                crossDomainEventRepository.save(event);

            } catch (Exception e) {
                handlePublishFailure(event, e);
            }
        }
    }

    private SendResult<String, Object> publishEvent(CrossDomainEvent event) {
        try {
            Object payload = objectMapper.readValue(event.getEventData(), Object.class);
            return kafkaTemplate.send(event.getKafkaTopic(), event.getEntityId(), payload)
                .get(publishTimeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new EventPublishException(String.format("Failed to publish event: eventUuid=%s, topic=%s", event.getEventUuid(), event.getKafkaTopic()), e);
        }
    }

    private void handlePublishFailure(CrossDomainEvent event, Exception e) {
        log.error("Event publish failed: eventUuid={}, topic={}, error={}", event.getEventUuid(), event.getKafkaTopic(), e.getMessage());

        event.incrementRetryCount();

        if (event.canRetry()) {
            crossDomainEventRepository.save(event);

        } else {
            event.markAsFailed(e.getMessage());
            crossDomainEventRepository.save(event);
        }
    }

}

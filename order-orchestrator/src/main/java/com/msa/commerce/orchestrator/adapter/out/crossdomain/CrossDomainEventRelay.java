package com.msa.commerce.orchestrator.adapter.out.crossdomain;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.orchestrator.application.port.out.CrossDomainEventRepository;
import com.msa.commerce.orchestrator.domain.crossdomain.CrossDomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossDomainEventRelay {

    private static final int BATCH_SIZE = 100;

    private static final int PUBLISH_TIMEOUT_SECONDS = 5;

    private final CrossDomainEventRepository crossDomainEventRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<CrossDomainEvent> pendingEvents = crossDomainEventRepository.findPendingEvents(BATCH_SIZE);

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

    private SendResult<String, Object> publishEvent(CrossDomainEvent event) throws Exception {
        Object payload = objectMapper.readValue(event.getEventData(), Object.class);

        return kafkaTemplate.send(event.getKafkaTopic(), event.getEntityId(), payload)
            .get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void handlePublishFailure(CrossDomainEvent event, Exception e) {
        event.incrementRetryCount();

        if (event.canRetry()) {
            crossDomainEventRepository.save(event);

        } else {
            event.markAsFailed(e.getMessage());
            crossDomainEventRepository.save(event);
        }
    }

}

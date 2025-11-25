package com.msa.commerce.orchestrator.adapter.out.outbox;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.orchestrator.application.port.out.OutboxEventRepository;
import com.msa.commerce.orchestrator.domain.outbox.OutboxEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventRelay {

    private static final int BATCH_SIZE = 100;

    private static final int MAX_RETRIES = 3;

    private static final int PUBLISH_TIMEOUT_SECONDS = 5;

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(BATCH_SIZE);

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
                event.markAsPublished();
                outboxEventRepository.save(event);

            } catch (Exception e) {
                handlePublishFailure(event, e);
            }
        }
    }

    private void publishEvent(OutboxEvent event) throws Exception {
        Object payload = objectMapper.readValue(event.getPayload(), Object.class);

        kafkaTemplate.send(event.getTopic(), event.getAggregateId(), payload)
            .get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void handlePublishFailure(OutboxEvent event, Exception e) {
        event.incrementRetryCount();

        if (event.canRetry(MAX_RETRIES)) {
            outboxEventRepository.save(event);

        } else {
            event.markAsFailed(e.getMessage());
            outboxEventRepository.save(event);
        }
    }

}

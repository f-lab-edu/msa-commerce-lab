package com.msa.commerce.orchestrator.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.msa.commerce.common.exception.kafka.EventPublishException;
import com.msa.commerce.orchestrator.application.port.out.EventPublisher;
import com.msa.commerce.orchestrator.domain.event.DomainEvent;
import com.msa.commerce.orchestrator.domain.event.EventValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(String topic, DomainEvent event) {
        publish(topic, event.getAggregateId(), event);
    }

    @Override
    public void publish(String topic, String key, DomainEvent event) {
        EventValidator.validate(event);

        log.info("Publishing event to topic: {}, key: {}, eventType: {}, eventId: {}", topic, key, event.getMetadata().getEventType(), event.getMetadata().getEventId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published event to topic: {}, partition: {}, offset: {}, eventId: {}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event.getMetadata().getEventId()
                );
            } else {
                log.error("Failed to publish event to topic: {}, eventId: {}, error: {}",
                    topic,
                    event.getMetadata().getEventId(),
                    ex.getMessage(),
                    ex
                );
                throw new EventPublishException("Failed to publish event to topic: " + topic, ex);
            }
        });
    }

}

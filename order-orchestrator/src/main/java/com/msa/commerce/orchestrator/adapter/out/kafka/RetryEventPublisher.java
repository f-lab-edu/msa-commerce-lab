package com.msa.commerce.orchestrator.adapter.out.kafka;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.domain.event.DomainEvent;
import com.msa.commerce.orchestrator.domain.event.FailedEvent;
import com.msa.commerce.orchestrator.domain.event.RetryableEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RetryEventPublisher {

    private static final int MAX_RETRIES = 3;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RetryEventPublisher(@Qualifier("objectKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public <T extends DomainEvent> void publishToRetryTopic(
        String originalTopic,
        Integer originalPartition,
        Long originalOffset,
        T event,
        Exception exception
    ) {
        String errorMessage = exception.getMessage();
        String stackTrace = getStackTraceAsString(exception);

        RetryableEvent<T> retryableEvent = RetryableEvent.create(
            originalTopic,
            originalPartition,
            originalOffset,
            MAX_RETRIES,
            event,
            errorMessage,
            stackTrace
        );

        String key = originalTopic + ":" + event.getMetadata().getEventId();

        kafkaTemplate.send(KafkaTopics.RETRY_EVENTS, key, retryableEvent)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent event to retry topic: eventId={}, retryCount={}",
                        event.getMetadata().getEventId(), retryableEvent.getRetryCount());
                } else {
                    log.error("Failed to send event to retry topic: eventId={}, error={}",
                        event.getMetadata().getEventId(), ex.getMessage(), ex);
                }
            });
    }

    public <T extends DomainEvent> void publishToDLQ(
        RetryableEvent<T> retryableEvent,
        String consumerGroup
    ) {
        FailedEvent<T> failedEvent = FailedEvent.from(retryableEvent, consumerGroup);

        String key = retryableEvent.getOriginalTopic() + ":" +
            retryableEvent.getPayload().getMetadata().getEventId();

        kafkaTemplate.send(KafkaTopics.DEAD_LETTER_QUEUE, key, failedEvent)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent event to DLQ: eventId={}, totalAttempts={}",
                        failedEvent.getPayload().getMetadata().getEventId(),
                        failedEvent.getTotalAttempts());
                } else {
                    log.error("Failed to send event to DLQ: eventId={}, error={}",
                        failedEvent.getPayload().getMetadata().getEventId(),
                        ex.getMessage(), ex);
                }
            });
    }

    private String getStackTraceAsString(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

}

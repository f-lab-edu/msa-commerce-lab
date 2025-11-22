package com.msa.commerce.orchestrator.adapter.in.kafka;

import java.time.LocalDateTime;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.adapter.out.kafka.RetryEventPublisher;
import com.msa.commerce.orchestrator.application.port.in.ProcessPaymentResultUseCase;
import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;
import com.msa.commerce.orchestrator.domain.event.RetryableEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryEventConsumer {

    private static final String CONSUMER_GROUP = "order-orchestrator-retry-group";

    private final ProcessPaymentResultUseCase processPaymentResultUseCase;

    private final RetryEventPublisher retryEventPublisher;

    @KafkaListener(
        topics = KafkaTopics.RETRY_EVENTS,
        groupId = CONSUMER_GROUP,
        containerFactory = "retryEventKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, RetryableEvent<?>> record, Acknowledgment ack) {
        RetryableEvent<?> retryableEvent = record.value();

        log.info("Received RetryableEvent: topic={}, partition={}, offset={}, retryCount={}/{}, originalTopic={}",
            record.topic(),
            record.partition(),
            record.offset(),
            retryableEvent.getRetryCount(),
            retryableEvent.getMaxRetries(),
            retryableEvent.getOriginalTopic()
        );

        if (!retryableEvent.canRetry()) {
            log.warn("Event exceeded max retries or not ready to retry: retryCount={}, maxRetries={}, nextRetryAt={}",
                retryableEvent.getRetryCount(),
                retryableEvent.getMaxRetries(),
                retryableEvent.getNextRetryAt());

            if (retryableEvent.getRetryCount() >= retryableEvent.getMaxRetries()) {
                retryEventPublisher.publishToDLQ(retryableEvent, CONSUMER_GROUP);
                log.info("Event moved to DLQ after {} retries", retryableEvent.getRetryCount());
            }

            ack.acknowledge();
            return;
        }

        if (LocalDateTime.now().isBefore(retryableEvent.getNextRetryAt())) {
            log.info("Event not ready for retry yet, will retry at: {}", retryableEvent.getNextRetryAt());
            return;
        }

        try {
            processRetryableEvent(retryableEvent);
            ack.acknowledge();
            log.info("Successfully processed RetryableEvent on retry attempt {}", retryableEvent.getRetryCount());

        } catch (Exception e) {
            log.error("Retry processing failed: retryCount={}, error={}",
                retryableEvent.getRetryCount(), e.getMessage(), e);

            RetryableEvent<?> updatedRetryableEvent = retryableEvent.incrementRetry(
                e.getMessage(),
                getStackTraceAsString(e)
            );

            if (updatedRetryableEvent.canRetry()) {
                retryEventPublisher.publishToRetryTopic(
                    updatedRetryableEvent.getOriginalTopic(),
                    updatedRetryableEvent.getOriginalPartition(),
                    updatedRetryableEvent.getOriginalOffset(),
                    updatedRetryableEvent.getPayload(),
                    e
                );
                log.info("Re-queued event for retry: retryCount={}", updatedRetryableEvent.getRetryCount());
            } else {
                retryEventPublisher.publishToDLQ(updatedRetryableEvent, CONSUMER_GROUP);
                log.info("Event moved to DLQ after failed retry");
            }

            ack.acknowledge();
        }
    }

    private void processRetryableEvent(RetryableEvent<?> retryableEvent) {
        Object payload = retryableEvent.getPayload();

        if (payload instanceof PaymentResultEvent paymentResultEvent) {
            processPaymentResultUseCase.processPaymentResult(paymentResultEvent);
        } else {
            throw new IllegalArgumentException(
                "Unsupported event type for retry: " + payload.getClass().getName()
            );
        }
    }

    private String getStackTraceAsString(Exception exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

}

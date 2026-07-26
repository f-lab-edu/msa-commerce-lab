package com.msa.commerce.payment.adapter.out.kafka;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.payment.application.port.out.OutboxEventRepository;
import com.msa.commerce.payment.domain.outbox.OutboxEvent;

import lombok.extern.slf4j.Slf4j;

// outbox 에 쌓인 PENDING 이벤트를 주기적으로 Kafka 로 밀어낸다.
@Slf4j
@Component
public class PaymentOutboxRelay {

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final int batchSize;

    private final int publishTimeoutSeconds;

    public PaymentOutboxRelay(
        OutboxEventRepository outboxEventRepository,
        KafkaTemplate<String, Object> kafkaTemplate,
        ObjectMapper objectMapper,
        @Value("${app.kafka.relay.batch-size:100}") int batchSize,
        @Value("${app.kafka.relay.publish-timeout-seconds:5}") int publishTimeoutSeconds) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.publishTimeoutSeconds = publishTimeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${app.kafka.relay.fixed-delay-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPending(batchSize);

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {
        try {
            // 저장된 payload 는 JSON 문자열이다. 그대로 보내면 JsonSerializer 가
            // 문자열 리터럴로 한 번 더 감싸버리므로 객체로 되돌린 뒤 발행한다.
            Object payload = objectMapper.readValue(event.getPayload(), Object.class);

            SendResult<String, Object> result = kafkaTemplate
                .send(event.getTopic(), event.getAggregateId(), payload)
                .get(publishTimeoutSeconds, TimeUnit.SECONDS);

            event.markAsPublished(result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            outboxEventRepository.save(event);

            log.debug("Outbox event published: eventId={}, topic={}, partition={}, offset={}",
                event.getEventId(), event.getTopic(),
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            recordFailure(event, e);
        } catch (Exception e) {
            recordFailure(event, e);
        }
    }

    private void recordFailure(OutboxEvent event, Exception cause) {
        event.markAsFailed(cause.getMessage());
        outboxEventRepository.save(event);

        if (event.isExhausted()) {
            log.error("Outbox event exhausted its retries and needs manual recovery: eventId={}, topic={}",
                event.getEventId(), event.getTopic(), cause);
        } else {
            log.warn("Outbox event publish failed, will retry: eventId={}, retryCount={}, reason={}",
                event.getEventId(), event.getRetryCount(), cause.getMessage());
        }
    }

}

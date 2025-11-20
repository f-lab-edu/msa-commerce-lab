package com.msa.commerce.orchestrator.adapter.in.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.adapter.out.kafka.RetryEventPublisher;
import com.msa.commerce.orchestrator.domain.event.DomainEvent;

import lombok.RequiredArgsConstructor;

@Component("kafkaErrorHandler")
@RequiredArgsConstructor
public class KafkaErrorHandler implements KafkaListenerErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandler.class);

    private final RetryEventPublisher retryEventPublisher;

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.error("Error in Kafka listener: {}", exception.getMessage(), exception);

        Object payload = message.getPayload();
        if (payload instanceof ConsumerRecord<?, ?> record) {
            handleConsumerRecordError(record, exception);
        }

        return null;
    }

    private void handleConsumerRecordError(ConsumerRecord<?, ?> record, Exception exception) {
        String topic = record.topic();
        int partition = record.partition();
        long offset = record.offset();
        Object value = record.value();

        log.error("Failed to process message: topic={}, partition={}, offset={}, error={}",
            topic, partition, offset, exception.getMessage());

        if (value instanceof DomainEvent event) {
            try {
                retryEventPublisher.publishToRetryTopic(
                    topic,
                    partition,
                    offset,
                    event,
                    exception
                );
                log.info("Sent failed event to retry topic: eventId={}, topic={}, partition={}, offset={}",
                    event.getMetadata().getEventId(), topic, partition, offset);
            } catch (Exception e) {
                log.error("Failed to send event to retry topic: eventId={}, error={}",
                    event.getMetadata().getEventId(), e.getMessage(), e);
            }
        }
    }

}

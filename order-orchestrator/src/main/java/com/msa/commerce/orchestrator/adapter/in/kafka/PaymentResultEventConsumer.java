package com.msa.commerce.orchestrator.adapter.in.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.application.port.in.ProcessPaymentResultUseCase;
import com.msa.commerce.orchestrator.application.service.IdempotencyService;
import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentResultEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultEventConsumer.class);

    private final ProcessPaymentResultUseCase processPaymentResultUseCase;

    private final IdempotencyService idempotencyService;

    @KafkaListener(
        topics = KafkaTopics.PAYMENT_RESULT,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "paymentResultKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, PaymentResultEvent> record, Acknowledgment ack) {
        PaymentResultEvent event = record.value();

        log.info("Received PaymentResultEvent: topic={}, partition={}, offset={}, key={}, eventId={}, orderId={}, paymentStatus={}",
            record.topic(),
            record.partition(),
            record.offset(),
            record.key(),
            event.getMetadata().getEventId(),
            event.getOrderId(),
            event.getPaymentStatus()
        );

        try {
            String eventId = event.getMetadata().getEventId();

            if (idempotencyService.isProcessed(eventId)) {
                log.info("Duplicate PaymentResultEvent detected, skipping: eventId={}, orderId={}",
                    eventId, event.getOrderId());
                ack.acknowledge();
                return;
            }

            processPaymentResultUseCase.processPaymentResult(event);
            idempotencyService.markAsProcessed(eventId);

            ack.acknowledge();
            log.info("Successfully processed PaymentResultEvent: eventId={}, orderId={}",
                eventId, event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process PaymentResultEvent: eventId={}, orderId={}, error={}",
                event.getMetadata().getEventId(),
                event.getOrderId(),
                e.getMessage(),
                e
            );

            throw new PaymentResultProcessingException(
                "Failed to process payment result event: " + event.getMetadata().getEventId(), e
            );
        }
    }

}

package com.msa.commerce.payment.adapter.out.event;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.payment.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.payment.application.port.out.OutboxEventRepository;
import com.msa.commerce.payment.application.port.out.PaymentEventPublisher;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.event.PaymentResultEvent;
import com.msa.commerce.payment.domain.outbox.OutboxEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 이벤트를 Kafka 로 바로 쏘지 않고 outbox 테이블에 적재한다.
 * 결제 상태 변경과 같은 트랜잭션에서 커밋되므로 "DB 는 반영됐는데 이벤트만 유실"이 생기지 않는다.
 * 실제 발행은 PaymentOutboxRelay 가 맡는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPaymentEventPublisher implements PaymentEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    @Override
    public void publishPaymentResult(Payment payment, String correlationId) {
        PaymentResultEvent event = PaymentResultEvent.from(payment, correlationId);

        OutboxEvent outboxEvent = OutboxEvent.pending(
            event.metadata().eventId(),
            PaymentResultEvent.EVENT_TYPE,
            payment.getOrderId().toString(),
            KafkaTopics.PAYMENT_RESULT,
            serialize(event),
            event.metadata().correlationId());

        outboxEventRepository.save(outboxEvent);

        log.debug("PaymentResultEvent staged in outbox: eventId={}, paymentId={}, status={}",
            outboxEvent.getEventId(), payment.getPaymentId(), event.paymentStatus());
    }

    private String serialize(PaymentResultEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize PaymentResultEvent", e);
        }
    }

}

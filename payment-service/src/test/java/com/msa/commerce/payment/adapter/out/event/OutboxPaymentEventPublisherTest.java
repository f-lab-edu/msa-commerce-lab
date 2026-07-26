package com.msa.commerce.payment.adapter.out.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.payment.application.port.out.OutboxEventRepository;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.event.PaymentResultEvent;
import com.msa.commerce.payment.domain.outbox.OutboxEvent;
import com.msa.commerce.payment.domain.outbox.PublishingStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxPaymentEventPublisher 테스트")
class OutboxPaymentEventPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private ObjectMapper objectMapper;

    private OutboxPaymentEventPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        publisher = new OutboxPaymentEventPublisher(outboxEventRepository, objectMapper);
    }

    @Test
    @DisplayName("결제 결과를 PENDING 상태의 outbox 레코드로 적재한다")
    void stagesEventInOutbox() throws Exception {
        Payment payment = capturedPayment();

        publisher.publishPaymentResult(payment, "corr-1");

        OutboxEvent staged = captureStagedEvent();
        assertThat(staged.getPublishingStatus()).isEqualTo(PublishingStatus.PENDING);
        assertThat(staged.getEventType()).isEqualTo(PaymentResultEvent.EVENT_TYPE);
        assertThat(staged.getTopic()).isEqualTo("payment.result");
        assertThat(staged.getCorrelationId()).isEqualTo("corr-1");

        // Kafka 키가 주문 ID 여야 같은 주문의 이벤트 순서가 보장된다
        assertThat(staged.getAggregateId()).isEqualTo(payment.getOrderId().toString());
    }

    @Test
    @DisplayName("payload 는 소비자가 읽을 수 있는 PaymentResultEvent JSON 이다")
    void payloadIsSerializedEvent() throws Exception {
        Payment payment = capturedPayment();

        publisher.publishPaymentResult(payment, "corr-1");

        OutboxEvent staged = captureStagedEvent();
        var payload = objectMapper.readTree(staged.getPayload());
        assertThat(payload.get("orderId").asText()).isEqualTo(payment.getOrderId().toString());
        assertThat(payload.get("paymentStatus").asText()).isEqualTo("SUCCESS");
        assertThat(payload.get("transactionId").asText()).isEqualTo("TXN-1");
    }

    @Test
    @DisplayName("outbox 의 eventId 와 payload 의 metadata.eventId 가 같아야 소비자 멱등성이 성립한다")
    void eventIdMatchesMetadata() throws Exception {
        publisher.publishPaymentResult(capturedPayment(), null);

        OutboxEvent staged = captureStagedEvent();
        var payload = objectMapper.readTree(staged.getPayload());
        assertThat(staged.getEventId()).isEqualTo(payload.get("metadata").get("eventId").asText());
    }

    private OutboxEvent captureStagedEvent() {
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        return captor.getValue();
    }

    private Payment capturedPayment() {
        Payment payment = Payment.request(UUID.randomUUID(), 1001L, new BigDecimal("15000.0000"), "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", null);
        payment.capture("EXT-1", "TXN-1", "0001");
        return payment;
    }

}

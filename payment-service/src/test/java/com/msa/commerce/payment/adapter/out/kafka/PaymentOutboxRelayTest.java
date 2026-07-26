package com.msa.commerce.payment.adapter.out.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.payment.application.port.out.OutboxEventRepository;
import com.msa.commerce.payment.domain.outbox.OutboxEvent;
import com.msa.commerce.payment.domain.outbox.PublishingStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentOutboxRelay 테스트")
class PaymentOutboxRelayTest {

    private static final String TOPIC = "payment.result";

    private static final String AGGREGATE_ID = "order-1";

    private static final String BROKER_ERROR = "broker unavailable";

    private static final String PAYLOAD = "{\"orderId\":\"order-1\",\"paymentStatus\":\"SUCCESS\"}";

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private PaymentOutboxRelay relay;

    @BeforeEach
    void setUp() {
        relay = new PaymentOutboxRelay(outboxEventRepository, kafkaTemplate, new ObjectMapper(), 100, 5);
    }

    @Test
    @DisplayName("적재된 이벤트가 없으면 Kafka 를 건드리지 않는다")
    void doesNothingWhenNoPendingEvents() {
        given(outboxEventRepository.findPending(100)).willReturn(List.of());

        relay.publishPendingEvents();

        then(kafkaTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("발행에 성공하면 PUBLISHED 로 갱신한다")
    void marksPublishedOnSuccess() {
        givenPending(pendingEvent());
        givenSendSucceeds();

        relay.publishPendingEvents();

        OutboxEvent saved = captureSaved();
        assertThat(saved.getPublishingStatus()).isEqualTo(PublishingStatus.PUBLISHED);
        assertThat(saved.getKafkaPartition()).isEqualTo(1);
        assertThat(saved.getKafkaOffset()).isEqualTo(42L);
    }

    @Test
    @DisplayName("payload 를 문자열이 아닌 객체로 되돌려 발행한다")
    void publishesPayloadAsObject() {
        givenPending(pendingEvent());
        givenSendSucceeds();

        relay.publishPendingEvents();

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq(TOPIC), eq(AGGREGATE_ID), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue())
            .isInstanceOf(Map.class)
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
            .containsEntry("paymentStatus", "SUCCESS");
    }

    @Test
    @DisplayName("발행에 실패하면 재시도 횟수만 올리고 PENDING 을 유지한다")
    void keepsPendingOnFailure() {
        givenPending(pendingEvent());
        given(kafkaTemplate.send(anyString(), anyString(), any()))
            .willReturn(CompletableFuture.failedFuture(new IllegalStateException(BROKER_ERROR)));

        relay.publishPendingEvents();

        OutboxEvent saved = captureSaved();
        assertThat(saved.getPublishingStatus()).isEqualTo(PublishingStatus.PENDING);
        assertThat(saved.getRetryCount()).isEqualTo(1);
        assertThat(saved.getErrorMessage()).contains(BROKER_ERROR);
    }

    @Test
    @DisplayName("재시도 한도를 넘긴 이벤트는 FAILED 로 떨어진다")
    void marksFailedWhenRetriesExhausted() {
        OutboxEvent event = pendingEvent();
        event.markAsFailed(BROKER_ERROR);
        event.markAsFailed(BROKER_ERROR);
        givenPending(event);
        given(kafkaTemplate.send(anyString(), anyString(), any()))
            .willReturn(CompletableFuture.failedFuture(new IllegalStateException(BROKER_ERROR)));

        relay.publishPendingEvents();

        assertThat(captureSaved().getPublishingStatus()).isEqualTo(PublishingStatus.FAILED);
    }

    @Test
    @DisplayName("payload 가 깨져 있어도 다른 이벤트 처리를 막지 않는다")
    void isolatesFailurePerEvent() {
        OutboxEvent broken = OutboxEvent.pending("event-broken", "PAYMENT_RESULT", "order-2",
            TOPIC, "not-json", "corr-2");
        givenPending(broken, pendingEvent());
        givenSendSucceeds();

        relay.publishPendingEvents();

        verify(outboxEventRepository, times(2)).save(any(OutboxEvent.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    private void givenPending(OutboxEvent... events) {
        given(outboxEventRepository.findPending(100)).willReturn(List.of(events));
    }

    private void givenSendSucceeds() {
        SendResult<String, Object> sendResult = new SendResult<>(
            new ProducerRecord<>(TOPIC, AGGREGATE_ID, PAYLOAD),
            new RecordMetadata(new TopicPartition(TOPIC, 1), 42L, 0, 0L, 0, 0));

        given(kafkaTemplate.send(anyString(), anyString(), any()))
            .willReturn(CompletableFuture.completedFuture(sendResult));
    }

    private OutboxEvent captureSaved() {
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private OutboxEvent pendingEvent() {
        return OutboxEvent.pending("event-1", "PAYMENT_RESULT", AGGREGATE_ID, TOPIC, PAYLOAD, "corr-1");
    }

}

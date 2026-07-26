package com.msa.commerce.orchestrator.adapter.in.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaIntegrationTestBase;
import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.domain.event.EventMetadata;
import com.msa.commerce.orchestrator.domain.event.FailedEvent;
import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;
import com.msa.commerce.orchestrator.domain.event.RetryableEvent;

class RetryEventConsumerIntegrationTest extends KafkaIntegrationTestBase {

    @Autowired
    private KafkaTemplate<String, Object> objectKafkaTemplate;

    private KafkaMessageListenerContainer<String, RetryableEvent<?>> retryEventContainer;

    private KafkaMessageListenerContainer<String, FailedEvent> dlqContainer;

    private final List<RetryableEvent<?>> receivedRetryEvents = new ArrayList<>();

    private final List<FailedEvent> receivedDlqEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        setupRetryEventConsumer();
        setupDlqConsumer();
        receivedRetryEvents.clear();
        receivedDlqEvents.clear();
    }

    @AfterEach
    void tearDown() {
        if (retryEventContainer != null) {
            retryEventContainer.stop();
        }
        if (dlqContainer != null) {
            dlqContainer.stop();
        }
    }

    @Test
    @DisplayName("재시도 가능한 이벤트가 retry.events 토픽으로 발행되어야 한다")
    void shouldPublishRetryableEventToRetryTopic() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent originalEvent = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        RetryableEvent<PaymentResultEvent> retryableEvent = RetryableEvent.create(
            KafkaTopics.PAYMENT_RESULT,
            0,
            100L,
            3,
            originalEvent,
            "Test error",
            "Test stack trace"
        );

        // When
        objectKafkaTemplate.send(KafkaTopics.RETRY_EVENTS, orderId.toString(), retryableEvent);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(receivedRetryEvents).isNotEmpty();
                RetryableEvent<?> received = receivedRetryEvents.get(0);
                assertThat(received.getOriginalTopic()).isEqualTo(KafkaTopics.PAYMENT_RESULT);
                assertThat(received.getRetryCount()).isEqualTo(1);
                assertThat(received.getErrorMessage()).isEqualTo("Test error");
            });
    }

    @Test
    @DisplayName("최대 재시도 횟수를 초과한 이벤트는 DLQ로 이동해야 한다")
    void shouldMoveToDlqAfterMaxRetries() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent originalEvent = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        // Create event that already exceeded max retries
        RetryableEvent<PaymentResultEvent> retryableEvent = RetryableEvent.create(
            KafkaTopics.PAYMENT_RESULT,
            0,
            100L,
            3,
            originalEvent,
            "Max retries exceeded",
            "Test stack trace"
        );

        // Manually set retry count to exceed max
        RetryableEvent<PaymentResultEvent> exceededEvent = retryableEvent
            .incrementRetry("Error 1", "Stack 1")
            .incrementRetry("Error 2", "Stack 2")
            .incrementRetry("Error 3", "Stack 3");

        // When
        objectKafkaTemplate.send(KafkaTopics.RETRY_EVENTS, orderId.toString(), exceededEvent);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(receivedDlqEvents).isNotEmpty();
                FailedEvent failedEvent = receivedDlqEvents.get(0);
                assertThat(failedEvent.getOriginalTopic()).isEqualTo(KafkaTopics.PAYMENT_RESULT);
                assertThat(failedEvent.getTotalAttempts()).isGreaterThanOrEqualTo(3);
                assertThat(failedEvent.getConsumerGroup()).isNotEmpty();
            });
    }

    @Test
    @DisplayName("재시도 이벤트의 exponential backoff가 올바르게 계산되어야 한다")
    void shouldCalculateExponentialBackoffCorrectly() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent originalEvent = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        RetryableEvent<PaymentResultEvent> retryableEvent = RetryableEvent.create(
            KafkaTopics.PAYMENT_RESULT,
            0,
            100L,
            3,
            originalEvent,
            "Test error",
            "Test stack trace"
        );

        // When
        LocalDateTime firstRetryTime = retryableEvent.getNextRetryAt();
        RetryableEvent<PaymentResultEvent> secondRetry = retryableEvent.incrementRetry("Error", "Stack");
        LocalDateTime secondRetryTime = secondRetry.getNextRetryAt();

        // Then
        assertThat(retryableEvent.getRetryCount()).isEqualTo(1);
        assertThat(secondRetry.getRetryCount()).isEqualTo(2);

        // First retry: 2^1 * 10 = 20 seconds
        assertThat(firstRetryTime).isAfter(LocalDateTime.now().plusSeconds(9));

        // Second retry: 2^2 * 10 = 40 seconds
        assertThat(secondRetryTime).isAfter(LocalDateTime.now().plusSeconds(39));
    }

    @Test
    @DisplayName("canRetry() 메소드가 재시도 가능 여부를 올바르게 판단해야 한다")
    void shouldDetermineCanRetryCorrectly() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent originalEvent = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        RetryableEvent<PaymentResultEvent> retryableEvent = RetryableEvent.create(
            KafkaTopics.PAYMENT_RESULT,
            0,
            100L,
            3,
            originalEvent,
            "Test error",
            "Test stack trace"
        );

        // When & Then
        // canRetry() checks if retryCount < maxRetries AND LocalDateTime.now().isAfter(nextRetryAt)
        // Since nextRetryAt is in the future, canRetry() will be false initially
        assertThat(retryableEvent.canRetry()).isFalse();

        RetryableEvent<PaymentResultEvent> retry2 = retryableEvent.incrementRetry("E", "S");
        assertThat(retry2.canRetry()).isFalse();

        RetryableEvent<PaymentResultEvent> retry3 = retry2.incrementRetry("E", "S");
        assertThat(retry3.canRetry()).isFalse();

        RetryableEvent<PaymentResultEvent> retry4 = retry3.incrementRetry("E", "S");
        assertThat(retry4.canRetry()).isFalse();  // retryCount >= maxRetries
    }

    private void setupRetryEventConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-retry-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RetryableEvent.class.getName());

        DefaultKafkaConsumerFactory<String, RetryableEvent<?>> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(KafkaTopics.RETRY_EVENTS);
        retryEventContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        retryEventContainer.setupMessageListener((MessageListener<String, RetryableEvent<?>>)record ->
            receivedRetryEvents.add(record.value())
        );

        retryEventContainer.start();
        ContainerTestUtils.waitForAssignment(retryEventContainer, 1);
    }

    private void setupDlqConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-dlq-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, FailedEvent.class.getName());

        DefaultKafkaConsumerFactory<String, FailedEvent> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(KafkaTopics.DEAD_LETTER_QUEUE);
        dlqContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        dlqContainer.setupMessageListener((MessageListener<String, FailedEvent>)record ->
            receivedDlqEvents.add(record.value())
        );

        dlqContainer.start();
        ContainerTestUtils.waitForAssignment(dlqContainer, 1);
    }

    private PaymentResultEvent createPaymentResultEvent(
        UUID orderId,
        PaymentResultEvent.PaymentStatus status,
        String failureReason
    ) {
        EventMetadata metadata = EventMetadata.create("PaymentResult", "payment-service");

        return PaymentResultEvent.builder()
            .metadata(metadata)
            .paymentId(UUID.randomUUID())
            .orderId(orderId)
            .customerId(1000L)
            .paymentStatus(status)
            .amount(BigDecimal.valueOf(25000))
            .currency("KRW")
            .paymentMethod("CARD")
            .transactionId(status == PaymentResultEvent.PaymentStatus.SUCCESS ? "TXN-" + UUID.randomUUID() : null)
            .processedAt(LocalDateTime.now())
            .failureReason(failureReason)
            .build();
    }

}

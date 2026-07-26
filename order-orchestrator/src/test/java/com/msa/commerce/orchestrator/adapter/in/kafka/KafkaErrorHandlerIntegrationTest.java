package com.msa.commerce.orchestrator.adapter.in.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaIntegrationTestBase;
import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.application.port.in.ProcessPaymentResultUseCase;
import com.msa.commerce.orchestrator.domain.event.EventMetadata;
import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;
import com.msa.commerce.orchestrator.domain.event.RetryableEvent;

class KafkaErrorHandlerIntegrationTest extends KafkaIntegrationTestBase {

    private final List<RetryableEvent<?>> receivedRetryEvents = new ArrayList<>();

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private ProcessPaymentResultUseCase processPaymentResultUseCase;

    private KafkaMessageListenerContainer<String, RetryableEvent<?>> retryEventContainer;

    @BeforeEach
    void setUp() {
        setupRetryEventConsumer();
        receivedRetryEvents.clear();
    }

    @AfterEach
    void tearDown() {
        if (retryEventContainer != null) {
            retryEventContainer.stop();
        }
    }

    @Test
    @DisplayName("Consumer에서 예외 발생 시 retry.events 토픽으로 이벤트가 발행되어야 한다")
    void shouldPublishToRetryTopicWhenConsumerFails() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent event = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        // Mock UseCase to throw exception
        doThrow(new RuntimeException("Test exception"))
            .when(processPaymentResultUseCase)
            .processPaymentResult(any(PaymentResultEvent.class));

        // When
        kafkaTemplate.send(KafkaTopics.PAYMENT_RESULT, orderId.toString(), event);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(receivedRetryEvents).isNotEmpty();
                RetryableEvent<?> retryEvent = receivedRetryEvents.get(0);
                assertThat(retryEvent.getOriginalTopic()).isEqualTo(KafkaTopics.PAYMENT_RESULT);
                assertThat(retryEvent.getRetryCount()).isEqualTo(1);
                assertThat(retryEvent.getErrorMessage()).contains("Test exception");
            });
    }

    @Test
    @DisplayName("Error handler가 실패 이벤트의 메타데이터를 올바르게 보존해야 한다")
    void shouldPreserveEventMetadataWhenPublishingToRetryTopic() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent event = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        String originalEventId = event.getMetadata().getEventId();
        String originalCorrelationId = event.getMetadata().getCorrelationId();

        // Mock UseCase to throw exception
        doThrow(new RuntimeException("Metadata preservation test"))
            .when(processPaymentResultUseCase)
            .processPaymentResult(any(PaymentResultEvent.class));

        // When
        kafkaTemplate.send(KafkaTopics.PAYMENT_RESULT, orderId.toString(), event);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(receivedRetryEvents).isNotEmpty();
                RetryableEvent<?> retryEvent = receivedRetryEvents.get(0);

                Object payload = retryEvent.getPayload();
                assertThat(payload).isInstanceOf(PaymentResultEvent.class);

                PaymentResultEvent paymentEvent = (PaymentResultEvent)payload;
                assertThat(paymentEvent.getMetadata().getEventId()).isEqualTo(originalEventId);
                assertThat(paymentEvent.getMetadata().getCorrelationId()).isEqualTo(originalCorrelationId);
                assertThat(paymentEvent.getOrderId()).isEqualTo(orderId);
            });
    }

    @Test
    @DisplayName("Error handler가 파티션과 오프셋 정보를 올바르게 기록해야 한다")
    void shouldRecordPartitionAndOffsetInformation() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent event = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        // Mock UseCase to throw exception
        doThrow(new RuntimeException("Partition offset test"))
            .when(processPaymentResultUseCase)
            .processPaymentResult(any(PaymentResultEvent.class));

        // When
        kafkaTemplate.send(KafkaTopics.PAYMENT_RESULT, orderId.toString(), event);

        // Then
        await().atMost(15, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertThat(receivedRetryEvents).isNotEmpty();
                RetryableEvent<?> retryEvent = receivedRetryEvents.get(0);
                assertThat(retryEvent.getOriginalPartition()).isGreaterThanOrEqualTo(0);
                assertThat(retryEvent.getOriginalOffset()).isGreaterThanOrEqualTo(0);
            });
    }

    private void setupRetryEventConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-error-handler-retry-group");
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

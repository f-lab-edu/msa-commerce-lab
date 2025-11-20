package com.msa.commerce.orchestrator.adapter.in.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;

import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaIntegrationTestBase;
import com.msa.commerce.orchestrator.adapter.out.kafka.KafkaTopics;
import com.msa.commerce.orchestrator.application.port.in.ProcessPaymentResultUseCase;
import com.msa.commerce.orchestrator.domain.event.EventMetadata;
import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;

class PaymentResultEventConsumerIntegrationTest extends KafkaIntegrationTestBase {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private ProcessPaymentResultUseCase processPaymentResultUseCase;

    @Test
    @DisplayName("결제 성공 이벤트를 수신하고 처리해야 한다")
    void shouldConsumeAndProcessSuccessfulPaymentResult() {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentResultEvent event = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.SUCCESS,
            null
        );

        // When
        kafkaTemplate.send(KafkaTopics.PAYMENT_RESULT, orderId.toString(), event);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                ArgumentCaptor<PaymentResultEvent> captor = ArgumentCaptor.forClass(PaymentResultEvent.class);
                verify(processPaymentResultUseCase).processPaymentResult(captor.capture());

                PaymentResultEvent capturedEvent = captor.getValue();
                assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
                assertThat(capturedEvent.getPaymentStatus()).isEqualTo(PaymentResultEvent.PaymentStatus.SUCCESS);
            });
    }

    @Test
    @DisplayName("결제 실패 이벤트를 수신하고 처리해야 한다")
    void shouldConsumeAndProcessFailedPaymentResult() {
        // Given
        UUID orderId = UUID.randomUUID();
        String failureReason = "Insufficient funds";
        PaymentResultEvent event = createPaymentResultEvent(
            orderId,
            PaymentResultEvent.PaymentStatus.FAILED,
            failureReason
        );

        // When
        kafkaTemplate.send(KafkaTopics.PAYMENT_RESULT, orderId.toString(), event);

        // Then
        await().atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                ArgumentCaptor<PaymentResultEvent> captor = ArgumentCaptor.forClass(PaymentResultEvent.class);
                verify(processPaymentResultUseCase).processPaymentResult(captor.capture());

                PaymentResultEvent capturedEvent = captor.getValue();
                assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
                assertThat(capturedEvent.getPaymentStatus()).isEqualTo(PaymentResultEvent.PaymentStatus.FAILED);
                assertThat(capturedEvent.getFailureReason()).isEqualTo(failureReason);
            });
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

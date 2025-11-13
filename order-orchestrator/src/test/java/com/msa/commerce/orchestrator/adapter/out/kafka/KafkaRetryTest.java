package com.msa.commerce.orchestrator.adapter.out.kafka;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

class KafkaRetryTest {

    @Test
    void shouldHaveRetryableAnnotationOnPublishOrderCreatedEvent() throws NoSuchMethodException {
        Method method = KafkaOrderEventPublisher.class.getMethod("publishOrderCreatedEvent",
            com.msa.commerce.orchestrator.domain.Order.class);

        Retryable retryable = method.getAnnotation(Retryable.class);

        assertThat(retryable).isNotNull();
        assertThat(retryable.maxAttempts()).isEqualTo(3);
        assertThat(retryable.retryFor()).contains(RuntimeException.class);
    }

    @Test
    void shouldHaveRetryableAnnotationOnPublishOrderStatusChangedEvent() throws NoSuchMethodException {
        Method method = KafkaOrderEventPublisher.class.getMethod("publishOrderStatusChangedEvent",
            com.msa.commerce.orchestrator.domain.Order.class);

        Retryable retryable = method.getAnnotation(Retryable.class);

        assertThat(retryable).isNotNull();
        assertThat(retryable.maxAttempts()).isEqualTo(3);
        assertThat(retryable.retryFor()).contains(RuntimeException.class);
    }

    @Test
    void shouldHaveBackoffConfigurationOf1Second() throws NoSuchMethodException {
        Method method = KafkaOrderEventPublisher.class.getMethod("publishOrderCreatedEvent",
            com.msa.commerce.orchestrator.domain.Order.class);

        Retryable retryable = method.getAnnotation(Retryable.class);
        Backoff backoff = retryable.backoff();

        assertThat(backoff.delay()).isEqualTo(1000L);
    }

}

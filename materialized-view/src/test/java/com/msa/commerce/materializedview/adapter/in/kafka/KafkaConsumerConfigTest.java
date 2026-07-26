package com.msa.commerce.materializedview.adapter.in.kafka;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;

@DisplayName("KafkaConsumerConfig 단위 테스트")
class KafkaConsumerConfigTest {

    private final KafkaConsumerConfig config = new KafkaConsumerConfig(new KafkaProperties());

    @Test
    @DisplayName("리스너 컨테이너 팩토리가 에러 핸들러와 함께 생성된다")
    void createsListenerContainerFactories() {
        CommonErrorHandler errorHandler = errorHandler();

        assertThat(config.orderCreatedListenerContainerFactory(errorHandler)).isNotNull();
        assertThat(config.orderUpdatedListenerContainerFactory(errorHandler)).isNotNull();
    }

    @Test
    @DisplayName("에러 핸들러는 재시도 후 DLT로 발행하는 DefaultErrorHandler다")
    void createsDeadLetterErrorHandler() {
        assertThat(errorHandler()).isInstanceOf(DefaultErrorHandler.class);
    }

    @SuppressWarnings("unchecked")
    private KafkaTemplate<Object, Object> mockKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    private CommonErrorHandler errorHandler() {
        return config.kafkaViewErrorHandler(mockKafkaTemplate());
    }

}

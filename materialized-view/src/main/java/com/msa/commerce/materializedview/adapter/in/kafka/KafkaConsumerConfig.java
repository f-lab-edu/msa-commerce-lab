package com.msa.commerce.materializedview.adapter.in.kafka;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderCreatedEvent;
import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderUpdatedEvent;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private static final String TRUSTED_PACKAGE = "com.msa.commerce.materializedview.adapter.in.kafka.dto";

    private final KafkaProperties kafkaProperties;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedListenerContainerFactory(
        CommonErrorHandler kafkaViewErrorHandler) {
        return listenerContainerFactory(OrderCreatedEvent.class, kafkaViewErrorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent> orderUpdatedListenerContainerFactory(
        CommonErrorHandler kafkaViewErrorHandler) {
        return listenerContainerFactory(OrderUpdatedEvent.class, kafkaViewErrorHandler);
    }

    // 재시도 소진 시 DLT로 이관해 이벤트 유실 없이 장애를 격리한다.
    @Bean
    public CommonErrorHandler kafkaViewErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition(OrderEventTopics.DEAD_LETTER_QUEUE, -1));
        return new DefaultErrorHandler(recoverer, retryBackOff());
    }

    private ExponentialBackOff retryBackOff() {
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxAttempts(3);
        return backOff;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerContainerFactory(
        Class<T> eventType, CommonErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps(eventType)));
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    // 발행 측 클래스명이 담긴 타입 헤더를 무시하고, 토픽별 소비자 계약 타입으로 역직렬화한다.
    private Map<String, Object> consumerProps(Class<?> eventType) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, eventType.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, TRUSTED_PACKAGE);
        return props;
    }

}

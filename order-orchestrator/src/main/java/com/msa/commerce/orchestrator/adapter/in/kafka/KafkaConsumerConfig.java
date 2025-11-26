package com.msa.commerce.orchestrator.adapter.in.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.msa.commerce.common.kafka.KafkaLoggingInterceptor;
import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;
import com.msa.commerce.orchestrator.domain.event.RetryableEvent;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final String TRUSTED_PACKAGES = "com.msa.commerce.orchestrator.domain.event";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, PaymentResultEvent> paymentResultConsumerFactory() {
        Map<String, Object> configProps = createBaseConsumerConfig(groupId);

        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentResultEvent.class.getName());

        // Performance tuning for high-throughput consumer
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentResultEvent> paymentResultKafkaListenerContainerFactory() {
        return createListenerContainerFactory(paymentResultConsumerFactory(), 3);
    }

    @Bean
    public ConsumerFactory<String, RetryableEvent<?>> retryEventConsumerFactory() {
        Map<String, Object> configProps = createBaseConsumerConfig(groupId + "-retry");
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RetryableEvent<?>> retryEventKafkaListenerContainerFactory() {
        return createListenerContainerFactory(retryEventConsumerFactory(), 1);
    }

    private Map<String, Object> createBaseConsumerConfig(String consumerGroupId) {
        Map<String, Object> configProps = new HashMap<>();

        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Consumer behavior
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // JsonDeserializer configuration - restrict to specific packages for security
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, TRUSTED_PACKAGES);
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Interceptor for logging
        configProps.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, KafkaLoggingInterceptor.class.getName());

        return configProps;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(
        ConsumerFactory<String, T> consumerFactory,
        int concurrency
    ) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

}

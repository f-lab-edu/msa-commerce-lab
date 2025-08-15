package com.msa.commerce.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

/**
 * Test configuration for embedded Kafka testing.
 * Provides Kafka configuration that works with EmbeddedKafkaBroker.
 */
@TestConfiguration
public class EmbeddedKafkaTestConfig {

    /**
     * Producer factory for embedded Kafka testing
     */
    @Bean
    @Primary
    public ProducerFactory<String, Object> embeddedKafkaProducerFactory(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> configProps = KafkaTestUtils.producerProps(embeddedKafka);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for embedded Kafka testing
     */
    @Bean
    @Primary
    public KafkaTemplate<String, Object> embeddedKafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Consumer factory for embedded Kafka testing
     */
    @Bean
    @Primary
    public ConsumerFactory<String, Object> embeddedKafkaConsumerFactory(EmbeddedKafkaBroker embeddedKafka,
                                                                         @Value("${test.kafka.group-id:test-group}") String groupId) {
        Map<String, Object> configProps = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafka);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Listener container factory for embedded Kafka testing
     */
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, Object> embeddedKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }
}
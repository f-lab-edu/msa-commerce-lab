package com.msa.commerce.common.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = {KafkaConfig.class})
@Import(EmbeddedKafkaTestConfig.class)
@EmbeddedKafka(
    partitions = 1,
    topics = {"test-topic", "send-receive-test"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:0",
        "port=0"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.group-id=test-group",
    "test.kafka.group-id=test-group"
})
@DirtiesContext
class KafkaConfigIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void shouldSendAndReceiveMessage() throws Exception {
        // Given
        String topicName = "send-receive-test";
        String testKey = "test-key";
        Map<String, Object> testMessage = new HashMap<>();
        testMessage.put("id", "123");
        testMessage.put("message", "Hello Kafka!");
        testMessage.put("timestamp", System.currentTimeMillis());

        // Create consumer before sending message
        var consumerProps = KafkaTestUtils.consumerProps("test-group-1", "true", embeddedKafka);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                         org.apache.kafka.common.serialization.StringDeserializer.class);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                         org.springframework.kafka.support.serializer.JsonDeserializer.class);
        consumerProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        var consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, Object>(consumerProps);
        consumer.subscribe(Collections.singleton(topicName));
        
        // Wait for consumer to be ready
        Thread.sleep(1000);

        // When - Send message and wait for it to be sent
        var sendResult = kafkaTemplate.send(topicName, testKey, testMessage);
        sendResult.get(5, TimeUnit.SECONDS); // Wait for send to complete

        // Then - Verify message is received
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var records = consumer.poll(Duration.ofMillis(1000));
                    assertThat(records.isEmpty()).isFalse();
                    
                    var record = records.iterator().next();
                    assertThat(record.key()).isEqualTo(testKey);
                    assertThat(record.value()).isInstanceOf(Map.class);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> receivedMessage = (Map<String, Object>) record.value();
                    assertThat(receivedMessage.get("id")).isEqualTo("123");
                    assertThat(receivedMessage.get("message")).isEqualTo("Hello Kafka!");
                    assertThat(receivedMessage.get("timestamp")).isNotNull();
                });

        consumer.close();
    }

    @Test
    void shouldHandleNullKeyMessage() throws Exception {
        // Given
        String topicName = "test-topic";
        Map<String, Object> testMessage = new HashMap<>();
        testMessage.put("id", "456");
        testMessage.put("message", "Message without key");

        // Create a test consumer
        var consumerProps = KafkaTestUtils.consumerProps("test-consumer-group-2", "true", embeddedKafka);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                         org.apache.kafka.common.serialization.StringDeserializer.class);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                         org.springframework.kafka.support.serializer.JsonDeserializer.class);
        consumerProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        var consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, Object>(consumerProps);
        consumer.subscribe(Collections.singleton(topicName));

        // When - Send message with null key
        kafkaTemplate.send(topicName, null, testMessage);

        // Then - Verify message is received
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var records = consumer.poll(Duration.ofMillis(100));
                    assertThat(records.isEmpty()).isFalse();
                    
                    var record = records.iterator().next();
                    assertThat(record.key()).isNull();
                    assertThat(record.value()).isInstanceOf(Map.class);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> receivedMessage = (Map<String, Object>) record.value();
                    assertThat(receivedMessage.get("id")).isEqualTo("456");
                    assertThat(receivedMessage.get("message")).isEqualTo("Message without key");
                });

        consumer.close();
    }

    @Test
    void shouldVerifyKafkaTemplateConfiguration() {
        // Verify that KafkaTemplate is properly configured
        assertThat(kafkaTemplate).isNotNull();
        assertThat(kafkaTemplate.getProducerFactory()).isNotNull();
        
        // Verify that we can access the producer configuration
        var producerFactory = kafkaTemplate.getProducerFactory();
        assertThat(producerFactory).isNotNull();
    }
}
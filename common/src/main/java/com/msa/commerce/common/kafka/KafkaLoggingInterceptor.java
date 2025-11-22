package com.msa.commerce.common.kafka;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaLoggingInterceptor implements ConsumerInterceptor<String, Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConsumerRecords<String, Object> onConsume(ConsumerRecords<String, Object> records) {
        records.forEach(record -> {
            try {
                Object value = record.value();
                if (value != null) {
                    String json = objectMapper.writeValueAsString(value);
                    JsonNode jsonNode = objectMapper.readTree(json);

                    JsonNode metadata = jsonNode.get("metadata");
                    if (metadata != null) {
                        String eventId = metadata.get("eventId").asText();
                        String correlationId = metadata.get("correlationId").asText();

                        MDC.put("eventId", eventId);
                        MDC.put("correlationId", correlationId);
                    }
                }
                log.debug("Consumed message from topic={}, partition={}, offset={}, key={}", record.topic(), record.partition(), record.offset(), record.key());

            } catch (Exception e) {
                log.warn("Failed to extract metadata from Kafka record", e);
            }
        });

        return records;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        offsets.forEach((topicPartition, offsetAndMetadata) ->
            log.debug("Committed offset for topic={}, partition={}, offset={}", topicPartition.topic(), topicPartition.partition(), offsetAndMetadata.offset())
        );

        MDC.clear();
    }

    @Override
    public void close() {
        MDC.clear();
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }

}

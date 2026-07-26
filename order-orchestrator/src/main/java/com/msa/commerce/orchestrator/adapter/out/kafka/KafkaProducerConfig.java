package com.msa.commerce.orchestrator.adapter.out.kafka;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

// RetryEventPublisher(@Qualifier("objectKafkaTemplate"))와 CrossDomainEventRelay가
// 요구하는 KafkaTemplate<String, Object> 빈. Boot 자동구성 템플릿은 <Object, Object>라
// 제네릭이 맞지 않아 별도로 정의한다.
@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, Object> objectKafkaTemplate(KafkaProperties kafkaProperties) {
        return new KafkaTemplate<>(
            new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null)));
    }

}

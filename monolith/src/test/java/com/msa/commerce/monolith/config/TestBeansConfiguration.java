package com.msa.commerce.monolith.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.common.monitoring.MetricsCollector;

@TestConfiguration  
@EnableJpaAuditing
public class TestBeansConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public MetricsCollector metricsCollector(ObjectMapper objectMapper) {
        return new MetricsCollector(objectMapper);
    }
}

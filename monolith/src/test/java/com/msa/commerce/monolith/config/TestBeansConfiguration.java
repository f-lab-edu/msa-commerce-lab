package com.msa.commerce.monolith.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.common.monitoring.MetricsCollector;

// @DataJpaTest 슬라이스는 설정 클래스를 스캔하지 않으므로 운영과 동일한 감사 설정을 명시적으로 가져온다.
@TestConfiguration
@Import(JpaAuditingConfig.class)
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

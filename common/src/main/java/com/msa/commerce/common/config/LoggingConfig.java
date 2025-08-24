package com.msa.commerce.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.common.logging.LoggingFilter;
import com.msa.commerce.common.monitoring.MetricsCollector;

@Configuration
public class LoggingConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration(
        ObjectMapper objectMapper,
        MetricsCollector metricsCollector) {

        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingFilter(objectMapper, metricsCollector));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("LoggingFilter");
        return registration;
    }

}

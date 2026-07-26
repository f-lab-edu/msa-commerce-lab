package com.msa.commerce.orchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.msa.commerce.common.config.RestClientFactory;
import com.msa.commerce.common.config.RestClientProperties;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(
        @Value("${external.product-service.url}") String baseUrl,
        @Value("${external.product-service.timeout-millis:3000}") long timeoutMillis) {

        return RestClientFactory.create(RestClientProperties.of(baseUrl, timeoutMillis));
    }

}

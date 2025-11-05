package com.msa.commerce.orchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.msa.commerce.common.config.RestClientFactory;
import com.msa.commerce.common.config.RestClientProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Order Orchestrator의 외부 서비스 RestClient 설정
 * common 모듈의 RestClientFactory를 활용하여 일관된 설정을 적용한다.
 */
@Configuration
@Slf4j
public class RestClientConfig {

    @Value("${external.product-service.url}")
    private String productServiceUrl;

    @Value("${external.product-service.timeout:10000}")
    private int timeout;

    @Bean
    public RestClient productRestClient() {
        log.info("Initializing ProductRestClient with base URL: {}", productServiceUrl);

        RestClientProperties properties = new RestClientProperties(
            productServiceUrl,
            timeout,
            timeout
        );

        return RestClientFactory.create(properties);
    }
}

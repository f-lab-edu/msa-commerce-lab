package com.msa.commerce.common.config;

import java.time.Duration;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestClientFactory {

    public static RestClient create(RestClientProperties properties) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeout()));

        return RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .defaultStatusHandler(
                HttpStatusCode::is4xxClientError,
                (request, response) -> log.error("Client error occurred: status={}, url={}", response.getStatusCode(), request.getURI())
            )
            .defaultStatusHandler(
                HttpStatusCode::is5xxServerError,
                (request, response) -> log.error("Server error occurred: status={}, url={}", response.getStatusCode(), request.getURI())
            )
            .build();
    }

    public static RestClient create(RestClientProperties properties,
        java.util.function.Consumer<RestClient.Builder> builder) {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeout()));

        RestClient.Builder restClientBuilder = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory);

        // 커스텀 설정 적용
        builder.accept(restClientBuilder);

        return restClientBuilder.build();
    }

}

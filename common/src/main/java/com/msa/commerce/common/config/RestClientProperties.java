package com.msa.commerce.common.config;

import java.time.Duration;

public record RestClientProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    public RestClientProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL is required");
        }
        connectTimeout = connectTimeout != null ? connectTimeout : DEFAULT_TIMEOUT;
        readTimeout = readTimeout != null ? readTimeout : DEFAULT_TIMEOUT;
    }

    public static RestClientProperties of(String baseUrl) {
        return new RestClientProperties(baseUrl, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
    }

    public static RestClientProperties of(String baseUrl, long timeoutMillis) {
        Duration timeout = Duration.ofMillis(timeoutMillis);
        return new RestClientProperties(baseUrl, timeout, timeout);
    }

}

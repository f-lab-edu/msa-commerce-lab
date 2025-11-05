package com.msa.commerce.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestClientProperties {

    private String baseUrl;

    private int connectTimeout = 10000; // 기본값: 10초

    private int readTimeout = 10000;    // 기본값: 10초

    public RestClientProperties(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RestClientProperties(String baseUrl, int connectTimeout, int readTimeout) {
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

}

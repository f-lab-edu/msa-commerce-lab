package com.msa.commerce.common.config;

import java.net.http.HttpClient;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public final class RestClientFactory {

    private RestClientFactory() {
    }

    public static RestClient create(RestClientProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory(properties))
            .build();
    }

    // 4xx/5xx는 RestClient 기본 동작(HttpClientErrorException/HttpServerErrorException)에 맡겨
    // 호출부가 상태 코드별로 분기할 수 있도록 한다.
    private static JdkClientHttpRequestFactory requestFactory(RestClientProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.connectTimeout())
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        return requestFactory;
    }

}

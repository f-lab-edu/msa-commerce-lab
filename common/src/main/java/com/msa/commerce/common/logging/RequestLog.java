package com.msa.commerce.common.logging;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestLog {

    private final String requestId;

    private final LocalDateTime timestamp;

    private final String method;

    private final String uri;

    private final String queryString;

    private final Map<String, String> headers;

    private final Map<String, String> pathParameters;

    private final String requestBody;

    private final String clientIp;

    private final String userAgent;

}

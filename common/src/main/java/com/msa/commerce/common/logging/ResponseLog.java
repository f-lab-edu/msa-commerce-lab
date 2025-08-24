package com.msa.commerce.common.logging;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseLog {

    private final String requestId;

    private final LocalDateTime timestamp;

    private final String method;

    private final String uri;

    private final int statusCode;

    private final String statusText;

    private final String responseBody;

    private final Map<String, String> headers;

    private final long duration;

    private final int contentLength;

}

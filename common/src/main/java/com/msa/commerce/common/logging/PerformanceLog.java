package com.msa.commerce.common.logging;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerformanceLog {

    private final String method;

    private final String uri;

    private final int statusCode;

    private final long duration;

    private final LocalDateTime timestamp;

}

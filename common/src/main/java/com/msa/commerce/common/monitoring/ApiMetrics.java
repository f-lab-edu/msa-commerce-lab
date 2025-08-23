package com.msa.commerce.common.monitoring;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiMetrics {

    private final String endpoint;

    private final int statusCode;

    private final long duration;

    private final LocalDateTime timestamp;

}

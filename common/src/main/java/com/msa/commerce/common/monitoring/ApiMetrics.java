package com.msa.commerce.common.monitoring;

import java.time.LocalDateTime;

public record ApiMetrics(String endpoint, int statusCode, long duration, LocalDateTime timestamp) {

    public static ApiMetrics withTimestampNow(String endpoint, int statusCode, long duration) {
        return new ApiMetrics(endpoint, statusCode, duration, LocalDateTime.now());
    }

}

package com.msa.commerce.common.monitoring;

import java.time.LocalDateTime;

public record CacheMetrics(long hits, long misses, double hitRate, LocalDateTime timestamp) {

    public static CacheMetrics withTimestampNow(long hits, long misses, double hitRate) {
        return new CacheMetrics(hits, misses, hitRate, LocalDateTime.now());
    }

}

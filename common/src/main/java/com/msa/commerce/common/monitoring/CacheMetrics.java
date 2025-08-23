package com.msa.commerce.common.monitoring;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CacheMetrics {

    private final long hits;

    private final long misses;

    private final double hitRate;

    private final LocalDateTime timestamp;

}

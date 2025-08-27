package com.msa.commerce.common.config.cache;

import java.time.Duration;

public enum CacheStrategy {

    SHORT_TERM(Duration.ofMinutes(5)),

    MEDIUM_TERM(Duration.ofMinutes(15)),

    LONG_TERM(Duration.ofHours(1)),

    VERY_LONG_TERM(Duration.ofHours(6)),

    DAILY(Duration.ofDays(1)),

    DEFAULT(Duration.ofMinutes(30));

    private final Duration ttl;

    CacheStrategy(Duration ttl) {
        this.ttl = ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

    public long getTtlInMinutes() {
        return ttl.toMinutes();
    }

    public long getTtlInSeconds() {
        return ttl.getSeconds();
    }

}

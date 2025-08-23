package com.msa.commerce.common.monitoring;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsCollector {

    private final ObjectMapper objectMapper;

    // API call counters
    private final Map<String, AtomicLong> apiCallCounters = new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> apiErrorCounters = new ConcurrentHashMap<>();

    // Response time tracking
    private final Map<String, ResponseTimeStats> responseTimeStats = new ConcurrentHashMap<>();

    // Cache metrics
    private final AtomicLong cacheHits = new AtomicLong(0);

    private final AtomicLong cacheMisses = new AtomicLong(0);

    public void recordApiCall(String method, String uri, int statusCode, long duration) {
        String endpoint = method + " " + uri;

        // Increment call counter
        apiCallCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();

        // Track errors
        if (statusCode >= 400) {
            apiErrorCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        }

        // Track response times
        responseTimeStats.computeIfAbsent(endpoint, k -> new ResponseTimeStats())
            .addResponseTime(duration);

        // Log metrics
        try {
            ApiMetrics metrics = ApiMetrics.builder()
                .endpoint(endpoint)
                .statusCode(statusCode)
                .duration(duration)
                .timestamp(LocalDateTime.now())
                .build();

            log.info("METRICS: {}", objectMapper.writeValueAsString(metrics));
        } catch (Exception e) {
            log.warn("Failed to log API metrics: {}", e.getMessage());
        }

        // Check for alerts
        checkAlerts(endpoint, statusCode, duration);
    }

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
        logCacheMetrics();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
        logCacheMetrics();
    }

    public long getApiCallCount(String endpoint) {
        return apiCallCounters.getOrDefault(endpoint, new AtomicLong(0)).get();
    }

    public long getApiErrorCount(String endpoint) {
        return apiErrorCounters.getOrDefault(endpoint, new AtomicLong(0)).get();
    }

    public double getErrorRate(String endpoint) {
        long totalCalls = getApiCallCount(endpoint);
        long errorCalls = getApiErrorCount(endpoint);

        if (totalCalls == 0)
            return 0.0;
        return (double)errorCalls / totalCalls * 100.0;
    }

    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;

        if (total == 0)
            return 0.0;
        return (double)hits / total * 100.0;
    }

    public ResponseTimeStats getResponseTimeStats(String endpoint) {
        return responseTimeStats.get(endpoint);
    }

    private void logCacheMetrics() {
        try {
            CacheMetrics metrics = CacheMetrics.builder()
                .hits(cacheHits.get())
                .misses(cacheMisses.get())
                .hitRate(getCacheHitRate())
                .timestamp(LocalDateTime.now())
                .build();

            log.info("CACHE_METRICS: {}", objectMapper.writeValueAsString(metrics));
        } catch (Exception e) {
            log.warn("Failed to log cache metrics: {}", e.getMessage());
        }
    }

    private void checkAlerts(String endpoint, int statusCode, long duration) {
        // Alert for high error rate (>5%)
        double errorRate = getErrorRate(endpoint);
        if (errorRate > 5.0) {
            logAlert("HIGH_ERROR_RATE", endpoint,
                String.format("Error rate: %.2f%%", errorRate));
        }

        // Alert for slow response (>3000ms)
        if (duration > 3000) {
            logAlert("SLOW_RESPONSE", endpoint,
                String.format("Response time: %dms", duration));
        }

        // Alert for server errors
        if (statusCode >= 500) {
            logAlert("SERVER_ERROR", endpoint,
                String.format("Status code: %d", statusCode));
        }
    }

    private void logAlert(String alertType, String endpoint, String message) {
        try {
            Alert alert = Alert.builder()
                .type(alertType)
                .endpoint(endpoint)
                .message(message)
                .severity(determineSeverity(alertType))
                .timestamp(LocalDateTime.now())
                .build();

            log.warn("ALERT: {}", objectMapper.writeValueAsString(alert));
        } catch (Exception e) {
            log.warn("Failed to log alert: {}", e.getMessage());
        }
    }

    private String determineSeverity(String alertType) {
        return switch (alertType) {
            case "SERVER_ERROR" -> "CRITICAL";
            case "HIGH_ERROR_RATE", "SLOW_RESPONSE" -> "WARNING";
            default -> "INFO";
        };
    }

    public static class ResponseTimeStats {

        private final AtomicLong count = new AtomicLong(0);

        private final AtomicLong total = new AtomicLong(0);

        private volatile long min = Long.MAX_VALUE;

        private volatile long max = Long.MIN_VALUE;

        public synchronized void addResponseTime(long duration) {
            count.incrementAndGet();
            total.addAndGet(duration);

            if (duration < min)
                min = duration;
            if (duration > max)
                max = duration;
        }

        public double getAverage() {
            long c = count.get();
            return c == 0 ? 0.0 : (double)total.get() / c;
        }

        public long getMin() {
            return min == Long.MAX_VALUE ? 0 : min;
        }

        public long getMax() {
            return max == Long.MIN_VALUE ? 0 : max;
        }

        public long getCount() {
            return count.get();
        }

    }

}

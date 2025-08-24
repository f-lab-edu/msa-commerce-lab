package com.msa.commerce.common.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsCollector Tests")
class MetricsCollectorTest {

    @Mock
    private ObjectMapper objectMapper;

    private MetricsCollector metricsCollector;

    @BeforeEach
    void setUp() throws Exception {
        metricsCollector = new MetricsCollector(objectMapper);
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");
    }

    @Test
    @DisplayName("Should record API call metrics correctly")
    void shouldRecordApiCallMetricsCorrectly() {
        // Given
        String method = "GET";
        String uri = "/api/products";
        int statusCode = 200;
        long duration = 150;

        // When
        metricsCollector.recordApiCall(method, uri, statusCode, duration);

        // Then
        String endpoint = method + " " + uri;
        assertEquals(1, metricsCollector.getApiCallCount(endpoint));
        assertEquals(0, metricsCollector.getApiErrorCount(endpoint));
        assertEquals(0.0, metricsCollector.getErrorRate(endpoint));
    }

    @Test
    @DisplayName("Should record API error metrics correctly")
    void shouldRecordApiErrorMetricsCorrectly() {
        // Given
        String method = "POST";
        String uri = "/api/products";
        int statusCode = 400;
        long duration = 250;
        String endpoint = method + " " + uri;

        // When
        metricsCollector.recordApiCall(method, uri, statusCode, duration);
        metricsCollector.recordApiCall(method, uri, 200, 150);
        metricsCollector.recordApiCall(method, uri, 500, 300);

        // Then
        assertEquals(3, metricsCollector.getApiCallCount(endpoint));
        assertEquals(2, metricsCollector.getApiErrorCount(endpoint));
        assertEquals(66.67, metricsCollector.getErrorRate(endpoint), 0.01);
    }

    @Test
    @DisplayName("Should track response time statistics")
    void shouldTrackResponseTimeStatistics() {
        // Given
        String method = "GET";
        String uri = "/api/products";
        String endpoint = method + " " + uri;

        // When
        metricsCollector.recordApiCall(method, uri, 200, 100);
        metricsCollector.recordApiCall(method, uri, 200, 200);
        metricsCollector.recordApiCall(method, uri, 200, 300);

        // Then
        var stats = metricsCollector.getResponseTimeStats(endpoint);
        assertNotNull(stats);
        assertEquals(3, stats.getCount());
        assertEquals(200.0, stats.getAverage(), 0.01);
        assertEquals(100, stats.getMin());
        assertEquals(300, stats.getMax());
    }

    @Test
    @DisplayName("Should record cache hit metrics correctly")
    void shouldRecordCacheHitMetricsCorrectly() {
        // Given & When
        metricsCollector.recordCacheHit();
        metricsCollector.recordCacheHit();
        metricsCollector.recordCacheMiss();

        // Then
        assertEquals(66.67, metricsCollector.getCacheHitRate(), 0.01);
    }

    @Test
    @DisplayName("Should record cache miss metrics correctly")
    void shouldRecordCacheMissMetricsCorrectly() {
        // Given & When
        metricsCollector.recordCacheMiss();
        metricsCollector.recordCacheMiss();
        metricsCollector.recordCacheHit();

        // Then
        assertEquals(33.33, metricsCollector.getCacheHitRate(), 0.01);
    }

    @Test
    @DisplayName("Should handle zero cache operations")
    void shouldHandleZeroCacheOperations() {
        // When & Then
        assertEquals(0.0, metricsCollector.getCacheHitRate());
    }

    @Test
    @DisplayName("Should handle zero API calls for error rate calculation")
    void shouldHandleZeroApiCallsForErrorRateCalculation() {
        // When & Then
        assertEquals(0.0, metricsCollector.getErrorRate("GET /nonexistent"));
    }

    @Test
    @DisplayName("Should handle JSON serialization errors gracefully")
    void shouldHandleJsonSerializationErrorsGracefully() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));

        // When & Then
        assertDoesNotThrow(() -> {
            metricsCollector.recordApiCall("GET", "/api/products", 200, 150);
            metricsCollector.recordCacheHit();
        });
    }

    @Test
    @DisplayName("Should track multiple endpoints independently")
    void shouldTrackMultipleEndpointsIndependently() {
        // Given
        String endpoint1 = "GET /api/products";
        String endpoint2 = "POST /api/orders";

        // When
        metricsCollector.recordApiCall("GET", "/api/products", 200, 100);
        metricsCollector.recordApiCall("GET", "/api/products", 404, 50);
        metricsCollector.recordApiCall("POST", "/api/orders", 201, 200);

        // Then
        assertEquals(2, metricsCollector.getApiCallCount(endpoint1));
        assertEquals(1, metricsCollector.getApiErrorCount(endpoint1));
        assertEquals(50.0, metricsCollector.getErrorRate(endpoint1));

        assertEquals(1, metricsCollector.getApiCallCount(endpoint2));
        assertEquals(0, metricsCollector.getApiErrorCount(endpoint2));
        assertEquals(0.0, metricsCollector.getErrorRate(endpoint2));
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void shouldHandleConcurrentAccessSafely() throws InterruptedException {
        // Given
        int threadCount = 10;
        int callsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < callsPerThread; j++) {
                    metricsCollector.recordApiCall("GET", "/api/test", 200, 100);
                    metricsCollector.recordCacheHit();
                    metricsCollector.recordCacheMiss();
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertEquals(threadCount * callsPerThread, metricsCollector.getApiCallCount("GET /api/test"));
        assertEquals(50.0, metricsCollector.getCacheHitRate(), 0.01);
    }

    @Test
    @DisplayName("Should trigger HIGH_ERROR_RATE alert when error rate exceeds 5%")
    void shouldTriggerHighErrorRateAlert() throws Exception {
        // Given
        String method = "POST";
        String uri = "/api/orders";
        String endpoint = method + " " + uri;

        for (int i = 0; i < 94; i++) {
            metricsCollector.recordApiCall(method, uri, 200, 100);
        }

        // When
        for (int i = 0; i < 6; i++) {
            metricsCollector.recordApiCall(method, uri, 400, 100);
        }

        // Then
        double errorRate = metricsCollector.getErrorRate(endpoint);
        assertTrue(errorRate > 5.0);
        assertEquals(6.0, errorRate, 0.01);

        verify(objectMapper, atLeast(100)).writeValueAsString(any());
    }

    @Test
    @DisplayName("Should trigger SLOW_RESPONSE alert when response time exceeds 3000ms")
    void shouldTriggerSlowResponseAlert() throws Exception {
        // Given
        String method = "GET";
        String uri = "/api/slow-endpoint";
        long slowDuration = 5000L; // 5 seconds

        // When
        metricsCollector.recordApiCall(method, uri, 200, slowDuration);

        // Then
        verify(objectMapper, atLeast(2)).writeValueAsString(any()); // One for metrics, one for alert
    }

    @Test
    @DisplayName("Should trigger SERVER_ERROR alert for 5xx status codes")
    void shouldTriggerServerErrorAlert() throws Exception {
        // Given
        String method = "GET";
        String uri = "/api/failing-endpoint";
        int serverErrorCode = 500;

        // When
        metricsCollector.recordApiCall(method, uri, serverErrorCode, 100);

        // Then
        verify(objectMapper, atLeast(2)).writeValueAsString(any()); // One for metrics, one for alert
    }

    @Test
    @DisplayName("Should handle cache metrics logging errors gracefully")
    void shouldHandleCacheMetricsLoggingErrorsGracefully() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any(CacheMetrics.class)))
            .thenThrow(new RuntimeException("Cache metrics JSON error"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            metricsCollector.recordCacheHit();
            metricsCollector.recordCacheMiss();
        });
    }

    @Test
    @DisplayName("Should handle alert logging errors gracefully")
    void shouldHandleAlertLoggingErrorsGracefully() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any(Alert.class)))
            .thenThrow(new RuntimeException("Alert JSON error"));

        // When & Then
        assertDoesNotThrow(() -> {
            metricsCollector.recordApiCall("GET", "/api/test", 500, 100); // Server error
            metricsCollector.recordApiCall("GET", "/api/test", 200, 5000); // Slow response
        });
    }

    @Test
    @DisplayName("Should handle ResponseTimeStats edge cases correctly")
    void shouldHandleResponseTimeStatsEdgeCases() {
        // Given
        String method = "PUT";
        String uri = "/api/edge-cases";
        String endpoint = method + " " + uri;

        // When
        metricsCollector.recordApiCall(method, uri, 200, 150);

        // Then
        var stats = metricsCollector.getResponseTimeStats(endpoint);
        assertNotNull(stats);
        assertEquals(1, stats.getCount());
        assertEquals(150.0, stats.getAverage(), 0.01);
        assertEquals(150, stats.getMin());
        assertEquals(150, stats.getMax());
    }

    @Test
    @DisplayName("Should return null for non-existent endpoint stats")
    void shouldReturnNullForNonExistentEndpointStats() {
        // When
        var stats = metricsCollector.getResponseTimeStats("NONEXISTENT /api/fake");

        // Then
        assertNull(stats);
    }

    @Test
    @DisplayName("Should trigger multiple alerts for same request")
    void shouldTriggerMultipleAlertsForSameRequest() throws Exception {
        // Given
        String method = "POST";
        String uri = "/api/problematic";

        for (int i = 0; i < 19; i++) {
            metricsCollector.recordApiCall(method, uri, 200, 100);
        }

        // When
        metricsCollector.recordApiCall(method, uri, 500, 4000);

        // Then
        verify(objectMapper, atLeast(22)).writeValueAsString(any()); // 20 metrics + 1 slow alert + 1 server error alert
    }

    @Test
    @DisplayName("Should handle extreme response times correctly")
    void shouldHandleExtremeResponseTimesCorrectly() {
        // Given
        String method = "GET";
        String uri = "/api/extreme";
        String endpoint = method + " " + uri;

        // When
        metricsCollector.recordApiCall(method, uri, 200, 1); // 1ms
        metricsCollector.recordApiCall(method, uri, 200, Long.MAX_VALUE / 1000); // Very large but reasonable

        // Then
        var stats = metricsCollector.getResponseTimeStats(endpoint);
        assertNotNull(stats);
        assertEquals(2, stats.getCount());
        assertEquals(1, stats.getMin());
        assertTrue(stats.getMax() > 1000000); // Very large number
        assertTrue(stats.getAverage() > 500000); // Should be very large average
    }

    @Test
    @DisplayName("Should handle boundary error rate cases")
    void shouldHandleBoundaryErrorRateCases() {
        // Given
        String method = "DELETE";
        String uri = "/api/boundary";
        String endpoint = method + " " + uri;

        // Test exactly 5% error rate (should not trigger alert)
        for (int i = 0; i < 95; i++) {
            metricsCollector.recordApiCall(method, uri, 200, 100);
        }
        for (int i = 0; i < 5; i++) {
            metricsCollector.recordApiCall(method, uri, 400, 100);
        }

        // Then
        assertEquals(5.0, metricsCollector.getErrorRate(endpoint), 0.01);

        // When
        metricsCollector.recordApiCall(method, uri, 400, 100);

        // Then
        assertTrue(metricsCollector.getErrorRate(endpoint) > 5.0);
    }

    @Test
    @DisplayName("Should handle boundary response time cases")
    void shouldHandleBoundaryResponseTimeCases() throws Exception {
        // Given
        String method = "PATCH";
        String uri = "/api/timing";

        // Test exactly 3000ms (should not trigger alert)
        metricsCollector.recordApiCall(method, uri, 200, 3000);

        // Test 3001ms (should trigger alert)
        metricsCollector.recordApiCall(method, uri, 200, 3001);

        // Verify both metrics and alert were logged
        verify(objectMapper, atLeast(3)).writeValueAsString(any()); // 2 metrics + 1 alert
    }

}

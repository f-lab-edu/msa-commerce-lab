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

    @Mock(lenient = true)
    private ObjectMapper objectMapper;
    
    private MetricsCollector metricsCollector;
    
    @BeforeEach
    void setUp() throws Exception {
        metricsCollector = new MetricsCollector(objectMapper);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");
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
        
        // When & Then - should not throw exception
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
}
package com.msa.commerce.common.logging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.common.monitoring.MetricsCollector;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingFilter Tests")
class LoggingFilterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MetricsCollector metricsCollector;

    private LoggingFilter loggingFilter;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter(objectMapper, metricsCollector);
    }

    @Test
    @DisplayName("Should log request and response for API calls")
    void shouldLogRequestAndResponseForApiCalls() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/products");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer token123");
        request.setContent("{\"name\":\"test\",\"password\":\"secret123\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");

        // When
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(metricsCollector, times(1)).recordApiCall(
            eq("POST"),
            eq("/api/products"),
            eq(200),
            anyLong()
        );
        verify(objectMapper, atLeast(1)).writeValueAsString(any());
    }

    @Test
    @DisplayName("Should skip logging for health check endpoints")
    void shouldSkipLoggingForHealthCheckEndpoints() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/actuator/health");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // When
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(metricsCollector, never()).recordApiCall(anyString(), anyString(), anyInt(), anyLong());
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    @DisplayName("Should skip logging for static resources")
    void shouldSkipLoggingForStaticResources() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/static/app.css");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // When
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(metricsCollector, never()).recordApiCall(anyString(), anyString(), anyInt(), anyLong());
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    @DisplayName("Should mask sensitive data in request body")
    void shouldMaskSensitiveDataInRequestBody() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/login");
        request.setContent("{\"username\":\"user\",\"password\":\"secret123\",\"token\":\"abc123\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");

        // When
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(objectMapper, atLeast(1)).writeValueAsString(argThat(logEntry -> {
            if (logEntry instanceof RequestLog requestLog) {
                String body = requestLog.getRequestBody();
                return body != null && body.contains("***") && !body.contains("secret123");
            }
            return true;
        }));
    }

    @Test
    @DisplayName("Should filter out sensitive headers")
    void shouldFilterOutSensitiveHeaders() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/products");
        request.addHeader("Authorization", "Bearer token123");
        request.addHeader("Cookie", "session=abc123");
        request.addHeader("Content-Type", "application/json");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");

        // When
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(objectMapper, atLeast(1)).writeValueAsString(argThat(logEntry -> {
            if (logEntry instanceof RequestLog requestLog) {
                var headers = requestLog.getHeaders();
                return headers != null &&
                    !headers.containsKey("Authorization") &&
                    !headers.containsKey("Cookie") &&
                    headers.containsKey("Content-Type");
            }
            return true;
        }));
    }

    @Test
    @DisplayName("Should handle request processing errors gracefully")
    void shouldHandleRequestProcessingErrorsGracefully() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/products");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON processing failed"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            loggingFilter.doFilterInternal(request, response, filterChain);
        });

        verify(metricsCollector, times(1)).recordApiCall(
            eq("POST"),
            eq("/api/products"),
            eq(200),
            anyLong()
        );
    }

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header")
    void shouldExtractClientIpFromXForwardedForHeader() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/products");
        request.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");

        // When
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(objectMapper, atLeast(1)).writeValueAsString(argThat(logEntry -> {
            if (logEntry instanceof RequestLog requestLog) {
                return "192.168.1.1".equals(requestLog.getClientIp());
            }
            return true;
        }));
    }

}

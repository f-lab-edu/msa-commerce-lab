package com.msa.commerce.common.logging;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.common.monitoring.MetricsCollector;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "requestId";

    private final ObjectMapper objectMapper;

    private final MetricsCollector metricsCollector;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (shouldSkipLogging(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        String requestId = generateRequestId();

        // Set MDC for request tracking
        MDC.put(REQUEST_ID_KEY, requestId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // Log request
            logRequest(requestWrapper, requestId, startTime);

            // Process request
            filterChain.doFilter(requestWrapper, responseWrapper);

            // Log response and collect metrics
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            logResponse(requestWrapper, responseWrapper, requestId, startTime, endTime);

            // Collect metrics
            metricsCollector.recordApiCall(
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                responseWrapper.getStatus(),
                duration
            );

        } finally {
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId, long startTime) {
        try {
            RequestLog requestLog = RequestLog.builder()
                .requestId(requestId)
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .queryString(request.getQueryString())
                .headers(extractHeaders(request))
                .pathParameters(extractPathParameters(request))
                .requestBody(extractRequestBody(request))
                .clientIp(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

            log.info("REQUEST: {}", objectMapper.writeValueAsString(requestLog));

        } catch (Exception e) {
            log.warn("Failed to log request: {}", e.getMessage());
        }
    }

    private void logResponse(ContentCachingRequestWrapper request,
        ContentCachingResponseWrapper response,
        String requestId,
        long startTime,
        long endTime) {
        try {
            long duration = endTime - startTime;
            int statusCode = response.getStatus();

            ResponseLog responseLog = ResponseLog.builder()
                .requestId(requestId)
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .statusCode(statusCode)
                .statusText(HttpStatus.valueOf(statusCode).getReasonPhrase())
                .responseBody(extractResponseBody(response))
                .headers(extractResponseHeaders(response))
                .duration(duration)
                .contentLength(response.getContentSize())
                .build();

            String logLevel = determineLogLevel(statusCode, duration);
            String logMessage = "RESPONSE: " + objectMapper.writeValueAsString(responseLog);

            switch (logLevel) {
                case "ERROR" -> log.error(logMessage);
                case "WARN" -> log.warn(logMessage);
                default -> log.info(logMessage);
            }

            // Log performance metrics
            logPerformanceMetrics(request.getMethod(), request.getRequestURI(), statusCode, duration);

        } catch (Exception e) {
            log.warn("Failed to log response: {}", e.getMessage());
        }
    }

    private void logPerformanceMetrics(String method, String uri, int statusCode, long duration) {
        PerformanceLog performanceLog = PerformanceLog.builder()
            .method(method)
            .uri(uri)
            .statusCode(statusCode)
            .duration(duration)
            .timestamp(LocalDateTime.now())
            .build();

        try {
            log.info("PERFORMANCE: {}", objectMapper.writeValueAsString(performanceLog));
        } catch (Exception e) {
            log.warn("Failed to log performance metrics: {}", e.getMessage());
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!isSensitiveHeader(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }

    private Map<String, String> extractResponseHeaders(ContentCachingResponseWrapper response) {
        Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            if (!isSensitiveHeader(headerName)) {
                headers.put(headerName, response.getHeader(headerName));
            }
        }
        return headers;
    }

    private Map<String, String> extractPathParameters(HttpServletRequest request) {
        Map<String, String> pathParams = new HashMap<>();

        // Extract path variables from request attributes (set by Spring)
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                pathParams.put(key, values[0]);
            }
        });

        return pathParams;
    }

    private String extractRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, request.getCharacterEncoding());
                // Mask sensitive data in body
                return maskSensitiveData(body);
            }
        } catch (Exception e) {
            log.debug("Failed to extract request body: {}", e.getMessage());
        }
        return null;
    }

    private String extractResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0 && content.length < 10000) { // Limit response body logging
                String body = new String(content, response.getCharacterEncoding());
                return maskSensitiveData(body);
            }
        } catch (Exception e) {
            log.debug("Failed to extract response body: {}", e.getMessage());
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String determineLogLevel(int statusCode, long duration) {
        if (statusCode >= 500) {
            return "ERROR";
        } else if (statusCode >= 400 || duration > 3000) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

    private boolean shouldSkipLogging(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/health") ||
            uri.contains("/actuator") ||
            uri.contains("/swagger") ||
            uri.contains("/api-docs") ||
            uri.endsWith(".css") ||
            uri.endsWith(".js") ||
            uri.endsWith(".ico");
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCase = headerName.toLowerCase();
        return lowerCase.contains("authorization") ||
            lowerCase.contains("cookie") ||
            lowerCase.contains("password") ||
            lowerCase.contains("secret") ||
            lowerCase.contains("token");
    }

    private String maskSensitiveData(String content) {
        if (content == null)
            return null;

        // Mask common sensitive fields in JSON
        return content.replaceAll("(\"password\"\\s*:\\s*\")[^\"]*\"", "$1***\"")
            .replaceAll("(\"secret\"\\s*:\\s*\")[^\"]*\"", "$1***\"")
            .replaceAll("(\"token\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
    }

}

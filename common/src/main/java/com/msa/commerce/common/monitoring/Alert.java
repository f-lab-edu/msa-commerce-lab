package com.msa.commerce.common.monitoring;

import java.time.LocalDateTime;

public record Alert(String type, String endpoint, String message, String severity, LocalDateTime timestamp) {

    public static Alert withSeverityFromType(String type, String endpoint, String message) {
        return new Alert(type, endpoint, message, determineSeverityStatic(type), LocalDateTime.now());
    }

    private static String determineSeverityStatic(String type) {
        return switch (type) {
            case "TIMEOUT", "DOWN" -> "CRITICAL";
            case "SLOW", "ERROR" -> "WARN";
            default -> "INFO";
        };
    }

}

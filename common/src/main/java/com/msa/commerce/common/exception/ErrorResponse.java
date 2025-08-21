package com.msa.commerce.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final String message;
    
    private final String code;
    
    private final LocalDateTime timestamp;
    
    private final String path;
    
    private final int status;
    
    private final List<ValidationError> validationErrors;
    
    private final String debugInfo;
    
    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String rejectedValue;
        private final String message;
    }
}
package com.msa.commerce.common.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex, HttpServletRequest request) {

        log.warn("Business exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .code(ex.getErrorCode())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
        DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("Duplicate resource exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .code(ex.getErrorCode())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.CONFLICT.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .code(ex.getErrorCode())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.NOT_FOUND.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ProductUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleProductUpdateNotAllowedException(
        ProductUpdateNotAllowedException ex, HttpServletRequest request) {

        log.warn("Product update not allowed exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .code(ex.getErrorCode())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.FORBIDDEN.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(NoChangesProvidedException.class)
    public ResponseEntity<ErrorResponse> handleNoChangesProvidedException(
        NoChangesProvidedException ex, HttpServletRequest request) {

        log.warn("No changes provided exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .code(ex.getErrorCode())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation exception occurred: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::createValidationError)
            .collect(Collectors.toList());

        String message = "Validation failed for " + validationErrors.size() + " field(s)";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .validationErrors(validationErrors)
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
        ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Constraint violation exception: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
            .stream()
            .map(this::createValidationError)
            .collect(Collectors.toList());

        String message = "Constraint validation failed for " + validationErrors.size() + " field(s)";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .validationErrors(validationErrors)
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("HTTP message not readable: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Invalid request body format")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .debugInfo(isDebugMode() ? ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage() : null)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Method argument type mismatch: {}", ex.getMessage());

        String message = String.format("Invalid value for parameter '%s'. Expected %s but received '%s'",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
            ex.getValue());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.BAD_REQUEST.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("HTTP method not supported: {}", ex.getMessage());

        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
            ex.getMethod(),
            String.join(", ", ex.getSupportedMethods() != null ? ex.getSupportedMethods() : new String[] {}));

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.METHOD_NOT_ALLOWED.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("No handler found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Requested resource not found")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.NOT_FOUND.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() : null)
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex, HttpServletRequest request) {

        log.error("Unexpected exception occurred", ex);

        String message = isDebugMode() ? ex.getMessage() : "An unexpected error occurred";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .debugInfo(isDebugMode() ? ex.getClass().getSimpleName() + ": " + ex.getMessage() : null)
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ErrorResponse.ValidationError createValidationError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
            .field(fieldError.getField())
            .rejectedValue(fieldError.getRejectedValue() != null ? fieldError.getRejectedValue().toString() : null)
            .message(fieldError.getDefaultMessage())
            .build();
    }

    private ErrorResponse.ValidationError createValidationError(ConstraintViolation<?> violation) {
        return ErrorResponse.ValidationError.builder()
            .field(violation.getPropertyPath().toString())
            .rejectedValue(violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null)
            .message(violation.getMessage())
            .build();
    }

    private boolean isDebugMode() {
        return "dev".equals(activeProfile) || "test".equals(activeProfile) || "local".equals(activeProfile);
    }

}

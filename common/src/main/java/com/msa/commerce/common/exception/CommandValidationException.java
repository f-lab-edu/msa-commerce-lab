package com.msa.commerce.common.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

public class CommandValidationException extends RuntimeException {

    private final String commandType;

    private final List<ValidationError> errors;

    public CommandValidationException(String commandType, Set<ConstraintViolation<Object>> violations) {
        super(buildMessage(commandType, violations));
        this.commandType = commandType;
        this.errors = violations.stream()
            .map(violation -> new ValidationError(
                violation.getPropertyPath().toString(),
                violation.getMessage(),
                violation.getInvalidValue()
            ))
            .toList();
    }

    public CommandValidationException(String commandType, String message) {
        super(message);
        this.commandType = commandType;
        this.errors = new ArrayList<>();
    }

    public CommandValidationException(String commandType, String message, Throwable cause) {
        super(message, cause);
        this.commandType = commandType;
        this.errors = new ArrayList<>();
    }

    private static String buildMessage(String commandType, Set<ConstraintViolation<Object>> violations) {
        StringBuilder message = new StringBuilder();
        message.append("Command validation failed for ").append(commandType).append(": ");

        violations.forEach(violation ->
            message.append("[")
                .append(violation.getPropertyPath())
                .append(": ")
                .append(violation.getMessage())
                .append("] ")
        );

        return message.toString();
    }

    public String getCommandType() {
        return commandType;
    }

    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Detailed validation error information
     */
    public static class ValidationError {

        private final String propertyPath;

        private final String message;

        private final Object invalidValue;

        public ValidationError(String propertyPath, String message, Object invalidValue) {
            this.propertyPath = propertyPath;
            this.message = message;
            this.invalidValue = invalidValue;
        }

        public String getPropertyPath() {
            return propertyPath;
        }

        public String getMessage() {
            return message;
        }

        public Object getInvalidValue() {
            return invalidValue;
        }

        @Override
        public String toString() {
            return String.format("ValidationError{propertyPath='%s', message='%s', invalidValue=%s}",
                propertyPath, message, invalidValue);
        }

    }

}

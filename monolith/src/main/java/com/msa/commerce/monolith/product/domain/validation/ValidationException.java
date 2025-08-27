package com.msa.commerce.monolith.product.domain.validation;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public ValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = List.copyOf(errors); // Defensive copy
    }

    public ValidationException(List<ValidationError> errors) {
        this(createMessage(errors), errors);
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public boolean hasMultipleErrors() {
        return errors.size() > 1;
    }

    public int getErrorCount() {
        return errors.size();
    }

    private static String createMessage(List<ValidationError> errors) {
        if (errors.isEmpty()) {
            return "Validation failed";
        }

        if (errors.size() == 1) {
            return "Validation failed: " + errors.get(0).getMessage();
        }

        return "Validation failed with " + errors.size() + " errors: " +
            errors.stream()
                .map(ValidationError::getMessage)
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }

}

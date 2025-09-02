package com.msa.commerce.monolith.product.domain.validation;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public ValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    public ValidationException(String message) {
        super(message);
        this.errors = List.of();
    }

    public ValidationException(Notification notification) {
        super(notification.getContext());
        this.errors = notification.getErrors();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int getErrorCount() {
        return errors.size();
    }

}

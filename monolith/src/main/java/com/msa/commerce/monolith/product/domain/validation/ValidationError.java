package com.msa.commerce.monolith.product.domain.validation;

import java.util.Objects;

public class ValidationError {

    private final String field;
    private final String message;

    public ValidationError(String field, String message) {
        this.field = field;
        this.message = Objects.requireNonNull(message, "Validation error message cannot be null");
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public boolean hasField() {
        return field != null && !field.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidationError that = (ValidationError) o;
        return Objects.equals(field, that.field) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, message);
    }

    @Override
    public String toString() {
        if (hasField()) {
            return field + ": " + message;
        }
        return message;
    }

}

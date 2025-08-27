package com.msa.commerce.monolith.product.domain.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Notification {

    private final List<ValidationError> errors;

    public Notification() {
        this.errors = new ArrayList<>();
    }

    public Notification addError(String field, String message) {
        errors.add(new ValidationError(field, message));
        return this;
    }

    public Notification addError(String message) {
        errors.add(new ValidationError(null, message));
        return this;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<String> getErrorMessages() {
        return errors.stream()
            .map(ValidationError::getMessage)
            .collect(Collectors.toList());
    }

    public String getErrorText() {
        return errors.stream()
            .map(ValidationError::getMessage)
            .collect(Collectors.joining("; "));
    }

    public void clear() {
        errors.clear();
    }

    public int getErrorCount() {
        return errors.size();
    }

    public void throwIfHasErrors() {
        if (hasErrors()) {
            throw new ValidationException(getErrorText(), errors);
        }
    }

    public static Notification success() {
        return new Notification();
    }

    @Override
    public String toString() {
        if (errors.isEmpty()) {
            return "Notification[success]";
        }
        return "Notification[errors=" + errors.size() + ", messages=" + getErrorText() + "]";
    }

}

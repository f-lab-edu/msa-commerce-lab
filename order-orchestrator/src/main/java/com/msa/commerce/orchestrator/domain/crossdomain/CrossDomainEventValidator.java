package com.msa.commerce.orchestrator.domain.crossdomain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CrossDomainEventValidator {

    private final List<ValidationRule> rules = new ArrayList<>();

    public static CrossDomainEventValidator builder() {
        return new CrossDomainEventValidator();
    }

    public CrossDomainEventValidator requireNonNull(Object value, String fieldName) {
        rules.add(new ValidationRule(
            () -> value != null,
            () -> fieldName + " cannot be null"
        ));
        return this;
    }

    public CrossDomainEventValidator requireNonEmpty(String value, String fieldName) {
        rules.add(new ValidationRule(
            () -> value != null && !value.trim().isEmpty(),
            () -> fieldName + " cannot be null or empty"
        ));
        return this;
    }

    public CrossDomainEventValidator requireNonEmpty(List<?> value, String fieldName) {
        rules.add(new ValidationRule(
            () -> value != null && !value.isEmpty(),
            () -> fieldName + " cannot be null or empty"
        ));
        return this;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        for (ValidationRule rule : rules) {
            if (!rule.condition.get()) {
                errors.add(rule.errorMessage.get());
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    private record ValidationRule(Supplier<Boolean> condition, Supplier<String> errorMessage) {

    }

}

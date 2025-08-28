package com.msa.commerce.monolith.product.domain.validation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "context", nullable = false, length = 200)
    private String context;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private final List<ValidationError> errors = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Notification() {
        // JPA only
    }

    public Notification(String context) {
        this.context = context;
        this.isValid = true;
    }

    public Notification addError(ValidationError error) {
        this.errors.add(error);
        this.isValid = false;
        return this;
    }

    public Notification addError(String fieldName, String errorMessage) {
        ValidationError error = ValidationError.builder()
            .fieldName(fieldName)
            .errorCode("VALIDATION_ERROR")
            .errorMessage(errorMessage)
            .rejectedValue(null)
            .build();
        return addError(error);
    }

    public Notification addError(String errorMessage) {
        ValidationError error = ValidationError.builder()
            .fieldName("general")
            .errorCode("GENERAL_ERROR")
            .errorMessage(errorMessage)
            .rejectedValue(null)
            .build();
        return addError(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public void throwIfHasErrors() {
        if (hasErrors()) {
            throw new ValidationException(this);
        }
    }

    public int getErrorCount() {
        return errors.size();
    }

    public List<String> getErrorMessages() {
        return errors.stream()
            .map(ValidationError::getErrorMessage)
            .toList();
    }

    public static Notification success() {
        return new Notification("Success");
    }

    public static Notification create() {
        return new Notification("Default validation");
    }

    public String getErrorText() {
        return errors.stream()
            .map(ValidationError::getErrorMessage)
            .reduce((a, b) -> a + "; " + b)
            .orElse("");
    }

    public void clear() {
        errors.clear();
        this.isValid = true;
    }

}


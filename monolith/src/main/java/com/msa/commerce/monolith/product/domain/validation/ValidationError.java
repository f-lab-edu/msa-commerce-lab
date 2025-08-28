package com.msa.commerce.monolith.product.domain.validation;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "validation_errors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "error_code", nullable = false, length = 50)
    private String errorCode;

    @Column(name = "error_message", nullable = false, length = 500)
    private String errorMessage;

    @Column(name = "rejected_value", length = 1000)
    private String rejectedValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ValidationError(String fieldName, String errorCode, String errorMessage, String rejectedValue) {
        this.fieldName = fieldName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return fieldName;
    }

    public String getMessage() {
        return errorMessage;
    }

}


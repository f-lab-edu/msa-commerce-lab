package com.msa.commerce.common.aop;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.common.exception.CommandValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@DisplayName("CommandValidationException - Exception Handling Tests")
class CommandValidationExceptionTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("CommandValidationException은 검증 오류 정보를 포함한다")
    void shouldContainValidationErrorInformation() {
        // Given
        TestCommand invalidCommand = TestCommand.builder()
            .id(null)
            .name("")
            .build();

        Set<ConstraintViolation<TestCommand>> violations = validator.validate(invalidCommand);
        
        // When
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> objectViolations = (Set<ConstraintViolation<Object>>) (Set<?>) violations;
        CommandValidationException exception = new CommandValidationException("TestCommand", objectViolations);

        // Then
        assertThat(exception.getCommandType()).isEqualTo("TestCommand");
        assertThat(exception.getErrors()).hasSize(2);
        assertThat(exception.getMessage()).contains("TestCommand");
        assertThat(exception.getMessage()).contains("validation failed");
    }

    @Test
    @DisplayName("ValidationError는 상세 오류 정보를 제공한다")
    void shouldProvideDetailedErrorInformation() {
        // Given
        TestCommand invalidCommand = TestCommand.builder()
            .id(null)
            .name("")
            .build();

        Set<ConstraintViolation<TestCommand>> violations = validator.validate(invalidCommand);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> objectViolations = (Set<ConstraintViolation<Object>>) (Set<?>) violations;
        CommandValidationException exception = new CommandValidationException("TestCommand", objectViolations);

        // When
        var errors = exception.getErrors();

        // Then
        assertThat(errors).allSatisfy(error -> {
            assertThat(error.getPropertyPath()).isNotBlank();
            assertThat(error.getMessage()).isNotBlank();
        });
    }

    @Test
    @DisplayName("단순 메시지로 CommandValidationException을 생성할 수 있다")
    void shouldCreateExceptionWithSimpleMessage() {
        // When
        CommandValidationException exception = new CommandValidationException(
            "TestCommand", 
            "Simple validation error"
        );

        // Then
        assertThat(exception.getCommandType()).isEqualTo("TestCommand");
        assertThat(exception.getMessage()).isEqualTo("Simple validation error");
        assertThat(exception.getErrors()).isEmpty();
    }

    @Getter
    @Builder
    static class TestCommand {
        @NotNull(message = "ID is required")
        private final Long id;

        @NotBlank(message = "Name is required")
        private final String name;
    }
}

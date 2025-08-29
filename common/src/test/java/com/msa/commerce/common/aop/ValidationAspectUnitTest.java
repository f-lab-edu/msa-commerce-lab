package com.msa.commerce.common.aop;

import com.msa.commerce.common.exception.CommandValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Builder;
import lombok.Getter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationAspect - Unit Tests")
class ValidationAspectUnitTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationAspect validationAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private Method testMethod;

    private TestCommand testCommand;

    @BeforeEach
    void setUp() throws Exception {
        testMethod = TestService.class.getMethod("processCommand", TestCommand.class);
        testCommand = TestCommand.builder()
                .id(1L)
                .name("Test")
                .build();

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);
    }

    @Test
    @DisplayName("유효한 Command 객체는 검증을 통과한다")
    void validCommandShouldPassValidation() throws Exception {
        // Given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testCommand});

        ValidateCommand annotation = mock(ValidateCommand.class);
        when(annotation.parameterIndices()).thenReturn(new int[]{});
        when(annotation.groups()).thenReturn(new Class<?>[]{});
        when(annotation.failFast()).thenReturn(false);
        when(annotation.includeParameterNames()).thenReturn(true);
        when(annotation.errorPrefix()).thenReturn("Test validation failed");

        when(validator.validate(any(), any(Class[].class))).thenReturn(new HashSet<>()); // Mock validator.validate to return an empty set (no violations)

        Method methodWithAnnotation = mock(Method.class);
        when(methodWithAnnotation.getAnnotation(ValidateCommand.class)).thenReturn(annotation);

        Parameter parameter = mock(Parameter.class);
        when(parameter.getName()).thenReturn("testParam");
        when(methodWithAnnotation.getParameters()).thenReturn(new Parameter[]{parameter});

        when(methodSignature.getMethod()).thenReturn(methodWithAnnotation);

        // When & Then
        validationAspect.validateCommandParameters(joinPoint);
    }

    @Test
    @DisplayName("검증 실패 시 CommandValidationException을 발생시킨다")
    void invalidCommandShouldThrowException() throws Exception {
        // Given
        when(joinPoint.getArgs()).thenReturn(new Object[]{testCommand});

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getMessage()).thenReturn("Test error");
        when(violation.getInvalidValue()).thenReturn("invalid");
        violations.add(violation);

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> mockViolations = (Set<ConstraintViolation<Object>>) (Set<?>) violations;
        when(validator.validate(any())).thenReturn(mockViolations);

        ValidateCommand annotation = mock(ValidateCommand.class);
        when(annotation.parameterIndices()).thenReturn(new int[]{});
        when(annotation.groups()).thenReturn(new Class<?>[]{});
        when(annotation.failFast()).thenReturn(false);
        when(annotation.includeParameterNames()).thenReturn(true);
        when(annotation.errorPrefix()).thenReturn("Test validation failed");

        Method methodWithAnnotation = mock(Method.class);
        when(methodWithAnnotation.getAnnotation(ValidateCommand.class)).thenReturn(annotation);
        when(methodWithAnnotation.getParameters()).thenReturn(testMethod.getParameters());
        when(methodSignature.getMethod()).thenReturn(methodWithAnnotation);

        // When & Then
        assertThatThrownBy(() -> validationAspect.validateCommandParameters(joinPoint))
                .isInstanceOf(CommandValidationException.class)
                .hasMessageContaining("Command validation failed");
    }

    @Test
    @DisplayName("Command가 아닌 객체는 검증에서 제외된다")
    void nonCommandObjectShouldBeIgnored() throws Exception {
        // Given
        String nonCommandObject = "not a command";
        when(joinPoint.getArgs()).thenReturn(new Object[]{nonCommandObject});

        ValidateCommand annotation = mock(ValidateCommand.class);
        when(annotation.parameterIndices()).thenReturn(new int[]{});

        Method methodWithAnnotation = mock(Method.class);
        when(methodWithAnnotation.getAnnotation(ValidateCommand.class)).thenReturn(annotation);
        when(methodSignature.getMethod()).thenReturn(methodWithAnnotation);

        // When & Then - should not throw exception
        validationAspect.validateCommandParameters(joinPoint);
    }

    static class TestService {

        @ValidateCommand
        public void processCommand(TestCommand command) {
            // Test method
        }

    }

    @Getter
    @Builder
    static class TestCommand {

        private final Long id;

        private final String name;

    }

}

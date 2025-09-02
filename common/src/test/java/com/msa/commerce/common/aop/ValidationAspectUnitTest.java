package com.msa.commerce.common.aop;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationAspect Unit Tests")
class ValidationAspectUnitTest {

    @Mock
    private Validator validator;
    
    @Mock
    private JoinPoint joinPoint;
    
    @Mock
    private MethodSignature methodSignature;
    
    @Mock
    private Method method;
    
    @Mock
    private ValidateResult validateResultAnnotation;
    
    @Mock
    @SuppressWarnings("rawtypes")
    private ConstraintViolation constraintViolation;
    
    private ValidationAspect validationAspect;
    
    @BeforeEach
    void setUp() {
        validationAspect = new ValidationAspect(validator);
        lenient().when(joinPoint.getSignature()).thenReturn(methodSignature);
        lenient().when(methodSignature.getMethod()).thenReturn(method);
        lenient().when(method.getAnnotation(ValidateResult.class)).thenReturn(validateResultAnnotation);
        lenient().when(method.getName()).thenReturn("testMethod");
    }
    
    @Test
    @DisplayName("Should skip validation when result is null")
    void shouldSkipValidationWhenResultIsNull() {
        // When
        validationAspect.validateMethodResult(joinPoint, null);
        
        // Then
        verify(validator, never()).validate(any());
        verify(validator, never()).validate(any(), any(Class[].class));
    }
    
    @Test
    @DisplayName("Should validate result when no validation target specified")
    void shouldValidateResultWhenNoValidationTargetSpecified() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[0]);
        when(validateResultAnnotation.groups()).thenReturn(new Class<?>[0]);
        when(validator.validate(testCommand)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator).validate(testCommand);
    }
    
    @Test
    @DisplayName("Should validate result when target class matches")
    void shouldValidateResultWhenTargetClassMatches() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{TestCommand.class});
        when(validateResultAnnotation.groups()).thenReturn(new Class<?>[0]);
        when(validator.validate(testCommand)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator).validate(testCommand);
    }
    
    @Test
    @DisplayName("Should skip validation when target class does not match")
    void shouldSkipValidationWhenTargetClassDoesNotMatch() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{String.class});
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator, never()).validate(any());
        verify(validator, never()).validate(any(), any(Class[].class));
    }
    
    @Test
    @DisplayName("Should validate with groups when groups specified")
    void shouldValidateWithGroupsWhenGroupsSpecified() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        Class<?>[] groups = {TestValidationGroup.class};
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[0]);
        when(validateResultAnnotation.groups()).thenReturn(groups);
        when(validator.validate(testCommand, groups)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator).validate(testCommand, groups);
    }
    
    @Test
    @DisplayName("Should throw ConstraintViolationException when validation fails")
    void shouldThrowConstraintViolationExceptionWhenValidationFails() {
        // Given
        TestCommand testCommand = new TestCommand("");
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add((ConstraintViolation<Object>) constraintViolation);
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[0]);
        when(validateResultAnnotation.groups()).thenReturn(new Class<?>[0]);
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<TestCommand>> mockViolations = (Set<ConstraintViolation<TestCommand>>) (Set<?>) violations;
        when(validator.validate(testCommand)).thenReturn(mockViolations);
        when(constraintViolation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(constraintViolation.getMessage()).thenReturn("Test validation error");
        
        // When & Then
        assertThatThrownBy(() -> validationAspect.validateMethodResult(joinPoint, testCommand))
            .isInstanceOf(ConstraintViolationException.class);
        
        verify(validator).validate(testCommand);
    }
    
    @Test
    @DisplayName("Should validate with both target class and groups")
    void shouldValidateWithBothTargetClassAndGroups() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        Class<?>[] groups = {TestValidationGroup.class};
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{TestCommand.class});
        when(validateResultAnnotation.groups()).thenReturn(groups);
        when(validator.validate(testCommand, groups)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator).validate(testCommand, groups);
    }
    
    @Test
    @DisplayName("Should handle multiple target classes - first match")
    void shouldHandleMultipleTargetClassesFirstMatch() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{TestCommand.class, String.class});
        when(validateResultAnnotation.groups()).thenReturn(new Class<?>[0]);
        when(validator.validate(testCommand)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator).validate(testCommand);
    }
    
    @Test
    @DisplayName("Should handle multiple target classes - second match")
    void shouldHandleMultipleTargetClassesSecondMatch() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{String.class, TestCommand.class});
        when(validateResultAnnotation.groups()).thenReturn(new Class<?>[0]);
        when(validator.validate(testCommand)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator).validate(testCommand);
    }
    
    @Test
    @DisplayName("Should handle inheritance in target class matching")
    void shouldHandleInheritanceInTargetClassMatching() {
        // Given
        ChildTestCommand childCommand = new ChildTestCommand("test", "extra");
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{TestCommand.class});
        when(validateResultAnnotation.groups()).thenReturn(new Class<?>[0]);
        when(validator.validate(childCommand)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, childCommand);
        
        // Then
        verify(validator).validate(childCommand);
    }
    
    @Test
    @DisplayName("Should handle multiple violations")
    void shouldHandleMultipleViolations() {
        // Given
        TestCommand testCommand = new TestCommand("");
        @SuppressWarnings("rawtypes")
        ConstraintViolation violation2 = mock(ConstraintViolation.class);
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add((ConstraintViolation<Object>) constraintViolation);
        violations.add((ConstraintViolation<Object>) violation2);
        
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[0]);
        when(validateResultAnnotation.groups()).thenReturn(new Class<?>[0]);
        
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<TestCommand>> mockViolations = (Set<ConstraintViolation<TestCommand>>) (Set<?>) violations;
        when(validator.validate(testCommand)).thenReturn(mockViolations);
        when(constraintViolation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(constraintViolation.getMessage()).thenReturn("First validation error");
        when(violation2.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation2.getMessage()).thenReturn("Second validation error");
        
        // When & Then
        assertThatThrownBy(() -> validationAspect.validateMethodResult(joinPoint, testCommand))
            .isInstanceOf(ConstraintViolationException.class);
        
        verify(validator).validate(testCommand);
    }
    
    @Test
    @DisplayName("Should not validate when target does not match - multiple targets")
    void shouldNotValidateWhenTargetDoesNotMatchMultipleTargets() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{String.class, Integer.class});
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator, never()).validate(any());
        verify(validator, never()).validate(any(), any(Class[].class));
    }
    
    @Test
    @DisplayName("Should validate with groups when target matches and groups specified")
    void shouldValidateWithGroupsWhenTargetMatchesAndGroupsSpecified() {
        // Given
        TestCommand testCommand = new TestCommand("test");
        Class<?>[] groups = {TestValidationGroup.class, AnotherValidationGroup.class};
        when(validateResultAnnotation.validationTarget()).thenReturn(new Class<?>[]{TestCommand.class});
        when(validateResultAnnotation.groups()).thenReturn(groups);
        when(validator.validate(testCommand, groups)).thenReturn(new HashSet<>());
        
        // When
        validationAspect.validateMethodResult(joinPoint, testCommand);
        
        // Then
        verify(validator).validate(testCommand, groups);
    }
    
    // Test helper classes
    public static class TestCommand {
        @SuppressWarnings("unused")
        private final String value;
        
        public TestCommand(String value) {
            this.value = value;
        }
    }
    
    public static class ChildTestCommand extends TestCommand {
        @SuppressWarnings("unused")
        private final String extraValue;
        
        public ChildTestCommand(String value, String extraValue) {
            super(value);
            this.extraValue = extraValue;
        }
    }
    
    public interface TestValidationGroup {
    }
    
    public interface AnotherValidationGroup {
    }
}

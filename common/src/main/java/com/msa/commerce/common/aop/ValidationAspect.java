package com.msa.commerce.common.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.msa.commerce.common.exception.CommandValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ValidationAspect {

    private final Validator validator;

    @Pointcut("@annotation(com.msa.commerce.common.aop.ValidateResult)")
    public void validateResultPointcut() {
    }

    @Pointcut("@annotation(com.msa.commerce.common.aop.ValidateCommand)")
    public void validateCommandPointcut() {
    }

    @Before("validateCommandPointcut()")
    public void validateCommandParameters(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        
        ValidateCommand annotation = method.getAnnotation(ValidateCommand.class);
        if (annotation == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Validating command parameters for method: {}", method.getName());
        }

        List<CommandValidationResult> validationResults = new ArrayList<>();

        // Determine which parameters to validate
        int[] indicesToValidate = annotation.parameterIndices();
        if (indicesToValidate.length == 0) {
            // Validate all parameters that are command objects
            indicesToValidate = findCommandParameterIndices(method, args);
        }

        // Validate specified parameters
        for (int index : indicesToValidate) {
            if (index >= 0 && index < args.length && args[index] != null) {
                Object commandObject = args[index];
                String parameterName = getParameterName(method, index, annotation.includeParameterNames());
                
                CommandValidationResult result = validateCommandObject(
                    commandObject, parameterName, annotation
                );
                
                if (!result.isValid()) {
                    validationResults.add(result);
                    
                    if (annotation.failFast()) {
                        throwValidationException(validationResults, annotation.errorPrefix());
                    }
                }
            }
        }

        // If not fail-fast and there are validation errors, throw exception
        if (!validationResults.isEmpty()) {
            throwValidationException(validationResults, annotation.errorPrefix());
        }

        if (log.isDebugEnabled()) {
            log.debug("Command validation passed for method: {}", method.getName());
        }
    }

    @AfterReturning(
        pointcut = "validateResultPointcut()",
        returning = "result"
    )
    public void validateMethodResult(JoinPoint joinPoint, Object result) {
        if (result == null) {
            return;
        }

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        ValidateResult annotation = method.getAnnotation(ValidateResult.class);

        if (log.isDebugEnabled()) {
            log.debug("Validating result of method: {} with value type: {}",
                method.getName(), result.getClass().getSimpleName());
        }

        if (annotation.validationTarget().length > 0) {
            boolean shouldValidate = false;
            for (Class<?> targetClass : annotation.validationTarget()) {
                if (targetClass.isInstance(result)) {
                    shouldValidate = true;
                    break;
                }
            }
            if (!shouldValidate) {
                return;
            }
        }

        // Validation 수행
        Set<ConstraintViolation<Object>> violations;
        if (annotation.groups().length > 0) {
            violations = validator.validate(result, annotation.groups());
        } else {
            violations = validator.validate(result);
        }

        // Violation이 있으면 예외 발생
        if (!violations.isEmpty()) {
            logViolations(method.getName(), violations);
            throw new ConstraintViolationException(violations);
        }

        if (log.isDebugEnabled()) {
            log.debug("Validation passed for method: {}", method.getName());
        }
    }

    private void logViolations(String methodName, Set<ConstraintViolation<Object>> violations) {
        if (log.isWarnEnabled()) {
            log.warn("Validation failed for method: {}. Violations:", methodName);
            violations.forEach(violation ->
                log.warn("  - {}: {}", violation.getPropertyPath(), violation.getMessage())
            );
        }
    }

    /**
     * Find parameter indices that are command objects
     * Command objects are identified by having "Command" in their class name
     */
    private int[] findCommandParameterIndices(Method method, Object[] args) {
        List<Integer> commandIndices = new ArrayList<>();
        
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && isCommandObject(args[i])) {
                commandIndices.add(i);
            }
        }
        
        return commandIndices.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Check if an object is a command object based on naming convention
     */
    private boolean isCommandObject(Object obj) {
        String className = obj.getClass().getSimpleName();
        return className.endsWith("Command") || className.endsWith("Cmd") || 
               className.contains("Command");
    }

    /**
     * Get parameter name for error reporting
     */
    private String getParameterName(Method method, int index, boolean includeParameterNames) {
        if (!includeParameterNames || index >= method.getParameters().length) {
            return "parameter[" + index + "]";
        }
        
        Parameter parameter = method.getParameters()[index];
        String parameterName = parameter.getName();
        
        // Fallback to type name if parameter name is generic
        if (parameterName.startsWith("arg")) {
            parameterName = parameter.getType().getSimpleName();
        }
        
        return parameterName;
    }

    /**
     * Validate a single command object
     */
    private CommandValidationResult validateCommandObject(Object commandObject, String parameterName, ValidateCommand annotation) {
        Set<ConstraintViolation<Object>> violations;
        
        if (annotation.groups().length > 0) {
            violations = validator.validate(commandObject, annotation.groups());
        } else {
            violations = validator.validate(commandObject);
        }
        
        if (violations.isEmpty()) {
            return new CommandValidationResult(parameterName, true, violations);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Command validation failed for {}: {} violations found", parameterName, violations.size());
                violations.forEach(violation ->
                    log.warn("  - {}.{}: {}", parameterName, violation.getPropertyPath(), violation.getMessage())
                );
            }
            return new CommandValidationResult(parameterName, false, violations);
        }
    }

    /**
     * Throw validation exception with detailed error information
     */
    private void throwValidationException(List<CommandValidationResult> validationResults, String errorPrefix) {
        StringBuilder errorMessage = new StringBuilder();
        
        if (!errorPrefix.isEmpty()) {
            errorMessage.append(errorPrefix).append(": ");
        }
        
        errorMessage.append("Command validation failed for parameters: ");
        
        List<String> commandTypes = new ArrayList<>();
        Set<ConstraintViolation<Object>> allViolations = new java.util.HashSet<>();
        
        for (CommandValidationResult result : validationResults) {
            commandTypes.add(result.getParameterName());
            allViolations.addAll(result.getViolations());
        }
        
        errorMessage.append(String.join(", ", commandTypes));
        
        throw new CommandValidationException(
            String.join(", ", commandTypes),
            allViolations
        );
    }

    /**
     * Internal class to hold validation results
     */
    private static class CommandValidationResult {
        private final String parameterName;
        private final boolean valid;
        private final Set<ConstraintViolation<Object>> violations;

        public CommandValidationResult(String parameterName, boolean valid, Set<ConstraintViolation<Object>> violations) {
            this.parameterName = parameterName;
            this.valid = valid;
            this.violations = violations;
        }

        public String getParameterName() {
            return parameterName;
        }

        public boolean isValid() {
            return valid;
        }

        public Set<ConstraintViolation<Object>> getViolations() {
            return violations;
        }
    }

}

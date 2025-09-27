package com.msa.commerce.common.aop;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;


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

        Set<ConstraintViolation<Object>> violations;
        if (annotation.groups().length > 0) {
            violations = validator.validate(result, annotation.groups());
        } else {
            violations = validator.validate(result);
        }

        if (!violations.isEmpty()) {
            logViolations(method.getName(), violations);
            throw new ConstraintViolationException(violations);
        }

        if (log.isDebugEnabled()) {
            log.debug("Validation passed for method: {}", method.getName());
        }
    }

    @Before("validateCommandPointcut()")
    public void validateMethodParameters(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ValidateCommand annotation = method.getAnnotation(ValidateCommand.class);

        if (args == null || args.length == 0) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Validating parameters for method: {}", method.getName());
        }

        for (Object arg : args) {
            if (arg == null) {
                continue;
            }

            // Validate using Bean Validation annotations
            Set<ConstraintViolation<Object>> violations;
            if (annotation.groups().length > 0) {
                violations = validator.validate(arg, annotation.groups());
            } else {
                violations = validator.validate(arg);
            }

            if (!violations.isEmpty()) {
                logViolations(method.getName(), violations);
                String errorMessage = formatErrorMessage(annotation.errorPrefix(), violations);
                throw new IllegalArgumentException(errorMessage);
            }

            // Custom validation using reflection for price range validation
            validateCustomBusinessRules(arg);
        }

        if (log.isDebugEnabled()) {
            log.debug("Parameter validation passed for method: {}", method.getName());
        }
    }

    private void validateCustomBusinessRules(Object command) {
        try {
            // Custom validation for price range using reflection
            Method getMinPrice = command.getClass().getMethod("getMinPrice");
            Method getMaxPrice = command.getClass().getMethod("getMaxPrice");
            
            Object minPrice = getMinPrice.invoke(command);
            Object maxPrice = getMaxPrice.invoke(command);
            
            if (minPrice != null && maxPrice != null && 
                minPrice instanceof BigDecimal && maxPrice instanceof BigDecimal) {
                BigDecimal min = (BigDecimal) minPrice;
                BigDecimal max = (BigDecimal) maxPrice;
                
                if (min.compareTo(max) > 0) {
                    throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            // Object doesn't have price range fields, skip custom validation
            if (log.isDebugEnabled()) {
                log.debug("Skipping price range validation for object type: {}", command.getClass().getSimpleName());
            }
        }
    }

    private String formatErrorMessage(String errorPrefix, Set<ConstraintViolation<Object>> violations) {
        StringBuilder sb = new StringBuilder();
        if (errorPrefix != null && !errorPrefix.isEmpty()) {
            sb.append(errorPrefix).append(": ");
        }
        
        violations.forEach(violation -> {
            if (sb.length() > 0 && !sb.toString().endsWith(": ")) {
                sb.append(", ");
            }
            sb.append(violation.getMessage());
        });
        
        return sb.toString();
    }

    private void logViolations(String methodName, Set<ConstraintViolation<Object>> violations) {
        if (log.isWarnEnabled()) {
            log.warn("Validation failed for method: {}. Violations:", methodName);
            violations.forEach(violation ->
                log.warn("  - {}: {}", violation.getPropertyPath(), violation.getMessage())
            );
        }
    }

}

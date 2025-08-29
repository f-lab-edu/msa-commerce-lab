package com.msa.commerce.common.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AOP annotation for generic command validation
 * Applies to methods that receive command objects as parameters
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateCommand {

    /**
     * Validation groups to apply
     */
    Class<?>[] groups() default {};

    /**
     * Specific parameter indices to validate (0-based)
     * If empty, validates all command objects in parameters
     */
    int[] parameterIndices() default {};

    /**
     * Whether to fail fast on first validation error
     */
    boolean failFast() default false;

    /**
     * Custom error message prefix
     */
    String errorPrefix() default "";

    /**
     * Whether to include parameter names in error messages
     */
    boolean includeParameterNames() default true;
}
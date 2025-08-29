package com.msa.commerce.common.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateCommand {

    Class<?>[] groups() default {};

    int[] parameterIndices() default {};

    boolean failFast() default false;

    String errorPrefix() default "";

    boolean includeParameterNames() default true;

}

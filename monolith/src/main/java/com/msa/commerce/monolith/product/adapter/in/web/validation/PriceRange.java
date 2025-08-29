package com.msa.commerce.monolith.product.adapter.in.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PriceRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PriceRange {

    String message() default "Minimum price cannot be greater than maximum price";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

package com.msa.commerce.monolith.product.adapter.in.web.validation;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.adapter.in.web.ProductSearchRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PriceRangeValidator implements ConstraintValidator<PriceRange, ProductSearchRequest> {

    @Override
    public void initialize(PriceRange constraintAnnotation) {
    }

    @Override
    public boolean isValid(ProductSearchRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        BigDecimal minPrice = request.getMinPrice();
        BigDecimal maxPrice = request.getMaxPrice();

        if (minPrice == null || maxPrice == null) {
            return true;
        }

        return minPrice.compareTo(maxPrice) <= 0;
    }

}

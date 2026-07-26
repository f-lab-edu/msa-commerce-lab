package com.msa.commerce.monolith.product.application.service;

import org.springframework.stereotype.Component;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ValidationException;
import com.msa.commerce.monolith.product.application.port.out.ProductCategoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductCategoryValidator {

    private final ProductCategoryRepository productCategoryRepository;

    public void validateActiveCategory(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        if (!productCategoryRepository.existsActiveById(categoryId)) {
            throw new ValidationException(
                "Category does not exist or is inactive: categoryId=%d".formatted(categoryId),
                ErrorCode.PRODUCT_CATEGORY_REQUIRED.getCode());
        }
    }

}

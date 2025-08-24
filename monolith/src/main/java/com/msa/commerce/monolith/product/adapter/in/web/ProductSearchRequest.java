package com.msa.commerce.monolith.product.adapter.in.web;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.domain.ProductStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductSearchRequest {

    private Long categoryId;

    @DecimalMin(value = "0.0", message = "Minimum price must be greater than or equal to 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maximum price must be greater than or equal to 0")
    private BigDecimal maxPrice;

    private ProductStatus status;

    @Min(value = 0, message = "Page must be greater than or equal to 0")
    @PositiveOrZero
    private Integer page = 0;

    @Min(value = 1, message = "Size must be greater than 0")
    @Max(value = 100, message = "Size must be less than or equal to 100")
    private Integer size = 20;

    private String sortBy = "createdAt";

    private String sortDirection = "desc";

    public void validatePriceRange() {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
    }
}
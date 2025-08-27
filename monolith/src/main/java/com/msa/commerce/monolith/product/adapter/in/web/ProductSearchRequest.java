package com.msa.commerce.monolith.product.adapter.in.web;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.adapter.in.web.validation.PriceRange;
import com.msa.commerce.monolith.product.domain.ProductStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@PriceRange
public class ProductSearchRequest {

    private Long categoryId;

    @DecimalMin(value = "0.0", message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maximum price cannot be negative")
    private BigDecimal maxPrice;

    private ProductStatus status;

    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must be at most 100")
    private Integer size = 20;

    private String sortBy = "createdAt";

    private String sortDirection = "desc";

}

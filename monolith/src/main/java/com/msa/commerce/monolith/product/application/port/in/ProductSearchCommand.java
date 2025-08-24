package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSearchCommand {

    private final Long categoryId;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final ProductStatus status;
    private final Integer page;
    private final Integer size;
    private final String sortBy;
    private final String sortDirection;

    public void validate() {
        if (page != null && page < 0) {
            throw new IllegalArgumentException("Page must be greater than or equal to 0");
        }
        
        if (size != null && (size < 1 || size > 100)) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
        
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }
        
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }
    }
}
package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;

import org.springframework.data.domain.Sort;

import com.msa.commerce.monolith.product.domain.ProductStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSearchCommand {

    private final Long categoryId;

    @DecimalMin(value = "0.0", message = "Minimum price cannot be negative")
    private final BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maximum price cannot be negative")
    private final BigDecimal maxPrice;

    private final ProductStatus status;

    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private final Integer page;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must be at most 100")
    private final Integer size;

    private final String sortProperty;

    private final Sort.Direction sortDirection;

}

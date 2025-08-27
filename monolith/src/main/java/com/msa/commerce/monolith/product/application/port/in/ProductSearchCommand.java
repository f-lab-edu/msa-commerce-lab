package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.validation.Notification;
import com.msa.commerce.monolith.product.domain.validation.ProductValidator;

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
        Notification notification = validateWithNotification();
        notification.throwIfHasErrors();
    }

    public Notification validateWithNotification() {
        return ProductValidator.validateProductSearch(
            page != null ? page : 0,
            size != null ? size : 20,
            minPrice,
            maxPrice
        );
    }

    public void validatePriceRange() {
        Notification notification = validateWithNotification();
        notification.throwIfHasErrors();
    }

}

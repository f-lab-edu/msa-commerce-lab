package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.domain.validation.Notification;
import com.msa.commerce.monolith.product.domain.validation.ProductValidator;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCreateCommand {

    private final Long categoryId;

    private final String sku;

    private final String name;

    private final String description;

    private final String shortDescription;

    private final String brand;

    private final String model;

    private final BigDecimal price;

    private final BigDecimal comparePrice;

    private final BigDecimal costPrice;

    private final BigDecimal weight;

    private final String productAttributes;

    private final String visibility;

    private final String taxClass;

    private final String metaTitle;

    private final String metaDescription;

    private final String searchKeywords;

    private final Boolean isFeatured;

    private final Integer initialStock;

    private final Integer lowStockThreshold;

    private final Boolean isTrackingEnabled;

    private final Boolean isBackorderAllowed;

    private final Integer minOrderQuantity;

    private final Integer maxOrderQuantity;

    private final Integer reorderPoint;

    private final Integer reorderQuantity;

    private final String locationCode;

    public void validate() {
        Notification notification = validateWithNotification();
        notification.throwIfHasErrors();
    }

    public Notification validateWithNotification() {
        return ProductValidator.validateProductCreation(
            categoryId, sku, name, price, description, shortDescription,
            brand, model, initialStock, lowStockThreshold, minOrderQuantity,
            maxOrderQuantity, reorderPoint, reorderQuantity, locationCode
        );
    }

}

package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;
import java.util.Optional;

import com.msa.commerce.monolith.product.domain.validation.Notification;
import com.msa.commerce.monolith.product.domain.validation.ProductValidator;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductUpdateCommand {

    private final Long productId;

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

    public Optional<Long> getCategoryIdOptional() {
        return Optional.ofNullable(categoryId);
    }

    public Optional<String> getSkuOptional() {
        return Optional.ofNullable(sku);
    }

    public Optional<String> getNameOptional() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getDescriptionOptional() {
        return Optional.ofNullable(description);
    }

    public Optional<String> getShortDescriptionOptional() {
        return Optional.ofNullable(shortDescription);
    }

    public Optional<String> getBrandOptional() {
        return Optional.ofNullable(brand);
    }

    public Optional<String> getModelOptional() {
        return Optional.ofNullable(model);
    }

    public Optional<BigDecimal> getPriceOptional() {
        return Optional.ofNullable(price);
    }

    public Optional<BigDecimal> getComparePriceOptional() {
        return Optional.ofNullable(comparePrice);
    }

    public Optional<BigDecimal> getCostPriceOptional() {
        return Optional.ofNullable(costPrice);
    }

    public Optional<BigDecimal> getWeightOptional() {
        return Optional.ofNullable(weight);
    }

    public Optional<String> getProductAttributesOptional() {
        return Optional.ofNullable(productAttributes);
    }

    public Optional<String> getVisibilityOptional() {
        return Optional.ofNullable(visibility);
    }

    public Optional<String> getTaxClassOptional() {
        return Optional.ofNullable(taxClass);
    }

    public Optional<String> getMetaTitleOptional() {
        return Optional.ofNullable(metaTitle);
    }

    public Optional<String> getMetaDescriptionOptional() {
        return Optional.ofNullable(metaDescription);
    }

    public Optional<String> getSearchKeywordsOptional() {
        return Optional.ofNullable(searchKeywords);
    }

    public Optional<Boolean> getIsFeaturedOptional() {
        return Optional.ofNullable(isFeatured);
    }

    public Optional<Integer> getInitialStockOptional() {
        return Optional.ofNullable(initialStock);
    }

    public Optional<Integer> getLowStockThresholdOptional() {
        return Optional.ofNullable(lowStockThreshold);
    }

    public Optional<Boolean> getIsTrackingEnabledOptional() {
        return Optional.ofNullable(isTrackingEnabled);
    }

    public Optional<Boolean> getIsBackorderAllowedOptional() {
        return Optional.ofNullable(isBackorderAllowed);
    }

    public Optional<Integer> getMinOrderQuantityOptional() {
        return Optional.ofNullable(minOrderQuantity);
    }

    public Optional<Integer> getMaxOrderQuantityOptional() {
        return Optional.ofNullable(maxOrderQuantity);
    }

    public Optional<Integer> getReorderPointOptional() {
        return Optional.ofNullable(reorderPoint);
    }

    public Optional<Integer> getReorderQuantityOptional() {
        return Optional.ofNullable(reorderQuantity);
    }

    public Optional<String> getLocationCodeOptional() {
        return Optional.ofNullable(locationCode);
    }

    public void validate() {
        Notification notification = validateWithNotification();
        notification.throwIfHasErrors();
    }

    public Notification validateWithNotification() {
        return ProductValidator.validateProductUpdate(
            productId, sku, name, price, description, shortDescription,
            brand, model, initialStock, lowStockThreshold, minOrderQuantity,
            maxOrderQuantity, reorderPoint, reorderQuantity, locationCode
        );
    }

    public boolean hasChanges() {
        return categoryId != null || sku != null || name != null || description != null ||
            shortDescription != null || brand != null || model != null || price != null ||
            comparePrice != null || costPrice != null || weight != null ||
            productAttributes != null || visibility != null || taxClass != null ||
            metaTitle != null || metaDescription != null || searchKeywords != null ||
            isFeatured != null || initialStock != null || lowStockThreshold != null ||
            isTrackingEnabled != null || isBackorderAllowed != null ||
            minOrderQuantity != null || maxOrderQuantity != null ||
            reorderPoint != null || reorderQuantity != null || locationCode != null;
    }

    public void setProductById(Long productId) {
    }

}


package com.msa.commerce.monolith.product.domain.validation;

import java.math.BigDecimal;
import java.util.Arrays;

import com.msa.commerce.monolith.product.domain.ProductCategory;

public class ProductValidator {
    
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");
    
    public static Notification validateProductCreation(
            Long categoryId,
            String sku, 
            String name, 
            BigDecimal price,
            String description,
            String shortDescription,
            String brand,
            String model,
            Integer initialStock,
            Integer lowStockThreshold,
            Integer minOrderQuantity,
            Integer maxOrderQuantity,
            Integer reorderPoint,
            Integer reorderQuantity,
            String locationCode) {
        
        Notification notification = new Notification();
        
        // Category validation
        validateCategory(notification, categoryId);
        
        // SKU validation
        validateSku(notification, sku);
        
        // Name validation
        validateName(notification, name);
        
        // Price validation
        validatePrice(notification, price);
        
        // Optional field validations
        validateDescription(notification, description);
        validateShortDescription(notification, shortDescription);
        validateBrand(notification, brand);
        validateModel(notification, model);
        validateInitialStock(notification, initialStock);
        validateLowStockThreshold(notification, lowStockThreshold);
        validateOrderQuantities(notification, minOrderQuantity, maxOrderQuantity);
        validateReorderPoint(notification, reorderPoint);
        validateReorderQuantity(notification, reorderQuantity);
        validateLocationCode(notification, locationCode);
        
        return notification;
    }
    
    public static Notification validateProductUpdate(
            Long productId,
            String sku,
            String name,
            BigDecimal price,
            String description,
            String shortDescription,
            String brand,
            String model,
            Integer initialStock,
            Integer lowStockThreshold,
            Integer minOrderQuantity,
            Integer maxOrderQuantity,
            Integer reorderPoint,
            Integer reorderQuantity,
            String locationCode) {
        
        Notification notification = new Notification();
        
        // Product ID is required for updates
        if (productId == null) {
            notification.addError("productId", "Product ID is required for update");
        }
        
        // Only validate fields that are being updated (not null)
        if (sku != null) {
            validateSku(notification, sku);
        }
        
        if (name != null) {
            validateName(notification, name);
        }
        
        if (price != null) {
            validatePrice(notification, price);
        }
        
        if (description != null) {
            validateDescription(notification, description);
        }
        
        if (shortDescription != null) {
            validateShortDescription(notification, shortDescription);
        }
        
        if (brand != null) {
            validateBrand(notification, brand);
        }
        
        if (model != null) {
            validateModel(notification, model);
        }
        
        if (initialStock != null) {
            validateInitialStock(notification, initialStock);
        }
        
        if (lowStockThreshold != null) {
            validateLowStockThreshold(notification, lowStockThreshold);
        }
        
        validateOrderQuantities(notification, minOrderQuantity, maxOrderQuantity);
        
        if (reorderPoint != null) {
            validateReorderPoint(notification, reorderPoint);
        }
        
        if (reorderQuantity != null) {
            validateReorderQuantity(notification, reorderQuantity);
        }
        
        if (locationCode != null) {
            validateLocationCode(notification, locationCode);
        }
        
        return notification;
    }
    
    public static Notification validateProductSearch(
            int page,
            int size,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        
        Notification notification = new Notification();
        
        if (page < 0) {
            notification.addError("page", "Page must be greater than or equal to 0");
        }
        
        if (size < 1 || size > 100) {
            notification.addError("size", "Size must be between 1 and 100");
        }
        
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            notification.addError("minPrice", "Minimum price cannot be negative");
        }
        
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            notification.addError("maxPrice", "Maximum price cannot be negative");
        }
        
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            notification.addError("priceRange", "Minimum price cannot be greater than maximum price");
        }
        
        return notification;
    }
    
    
    private static void validateCategory(Notification notification, Long categoryId) {
        if (categoryId == null) {
            notification.addError("categoryId", "Category ID is required");
            return;
        }
        
        boolean isValidCategory = Arrays.stream(ProductCategory.values())
            .anyMatch(category -> category.getId().equals(categoryId));
        if (!isValidCategory) {
            notification.addError("categoryId", "Invalid category ID: " + categoryId);
        }
    }
    
    private static void validateSku(Notification notification, String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            notification.addError("sku", "SKU is required");
            return;
        }
        
        if (sku.length() > 100) {
            notification.addError("sku", "SKU cannot exceed 100 characters");
        }
    }
    
    private static void validateName(Notification notification, String name) {
        if (name == null || name.trim().isEmpty()) {
            notification.addError("name", "Product name is required");
            return;
        }
        
        if (name.length() > 255) {
            notification.addError("name", "Product name cannot exceed 255 characters");
        }
    }
    
    private static void validatePrice(Notification notification, BigDecimal price) {
        if (price == null) {
            notification.addError("price", "Price is required");
            return;
        }
        
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            notification.addError("price", "Price must be greater than 0");
        }
        
        if (price.compareTo(MAX_PRICE) > 0) {
            notification.addError("price", "Price cannot exceed 99,999,999.99");
        }
    }
    
    private static void validateDescription(Notification notification, String description) {
        if (description != null && description.length() > 5000) {
            notification.addError("description", "Product description cannot exceed 5000 characters");
        }
    }
    
    private static void validateShortDescription(Notification notification, String shortDescription) {
        if (shortDescription != null && shortDescription.length() > 500) {
            notification.addError("shortDescription", "Short description cannot exceed 500 characters");
        }
    }
    
    private static void validateBrand(Notification notification, String brand) {
        if (brand != null && brand.length() > 100) {
            notification.addError("brand", "Brand cannot exceed 100 characters");
        }
    }
    
    private static void validateModel(Notification notification, String model) {
        if (model != null && model.length() > 100) {
            notification.addError("model", "Model cannot exceed 100 characters");
        }
    }
    
    private static void validateInitialStock(Notification notification, Integer initialStock) {
        if (initialStock != null && initialStock < 0) {
            notification.addError("initialStock", "Initial stock cannot be negative");
        }
    }
    
    private static void validateLowStockThreshold(Notification notification, Integer lowStockThreshold) {
        if (lowStockThreshold != null && lowStockThreshold < 0) {
            notification.addError("lowStockThreshold", "Low stock threshold cannot be negative");
        }
    }
    
    private static void validateOrderQuantities(Notification notification, Integer minOrderQuantity, Integer maxOrderQuantity) {
        if (minOrderQuantity != null && minOrderQuantity <= 0) {
            notification.addError("minOrderQuantity", "Minimum order quantity must be positive");
        }
        
        if (maxOrderQuantity != null && minOrderQuantity != null && maxOrderQuantity < minOrderQuantity) {
            notification.addError("maxOrderQuantity", "Maximum order quantity cannot be less than minimum order quantity");
        }
    }
    
    private static void validateReorderPoint(Notification notification, Integer reorderPoint) {
        if (reorderPoint != null && reorderPoint < 0) {
            notification.addError("reorderPoint", "Reorder point cannot be negative");
        }
    }
    
    private static void validateReorderQuantity(Notification notification, Integer reorderQuantity) {
        if (reorderQuantity != null && reorderQuantity < 0) {
            notification.addError("reorderQuantity", "Reorder quantity cannot be negative");
        }
    }
    
    private static void validateLocationCode(Notification notification, String locationCode) {
        if (locationCode != null && locationCode.trim().isEmpty()) {
            notification.addError("locationCode", "Location code cannot be empty");
        }
    }

}

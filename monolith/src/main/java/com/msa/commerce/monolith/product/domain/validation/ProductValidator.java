package com.msa.commerce.monolith.product.domain.validation;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.domain.Product;

public class ProductValidator {

    private static final int MAX_NAME_LENGTH = 255;

    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private static final int MAX_SKU_LENGTH = 100;

    public Notification validate(Product product) {
        Notification notification = new Notification("Product validation");

        validateSku(product.getSku(), notification);
        validateName(product.getName(), notification);
        validateDescription(product.getDescription(), notification);
        validatePrice(product.getPrice(), notification);
        validateCategory(product.getCategoryId(), notification);

        return notification;
    }

    private void validateSku(String sku, Notification notification) {
        if (sku == null || sku.trim().isEmpty()) {
            notification.addError(ValidationError.builder()
                .fieldName("sku")
                .errorCode("REQUIRED")
                .errorMessage("SKU는 필수입니다")
                .rejectedValue(sku)
                .build());
        } else if (sku.length() > MAX_SKU_LENGTH) {
            notification.addError(ValidationError.builder()
                .fieldName("sku")
                .errorCode("LENGTH_EXCEEDED")
                .errorMessage("SKU는 " + MAX_SKU_LENGTH + "자를 초과할 수 없습니다")
                .rejectedValue(sku)
                .build());
        }
    }

    private void validateName(String name, Notification notification) {
        if (name == null || name.trim().isEmpty()) {
            notification.addError(ValidationError.builder()
                .fieldName("name")
                .errorCode("REQUIRED")
                .errorMessage("상품명은 필수입니다")
                .rejectedValue(name)
                .build());
        } else if (name.length() > MAX_NAME_LENGTH) {
            notification.addError(ValidationError.builder()
                .fieldName("name")
                .errorCode("LENGTH_EXCEEDED")
                .errorMessage("상품명은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다")
                .rejectedValue(name)
                .build());
        }
    }

    private void validateDescription(String description, Notification notification) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            notification.addError(ValidationError.builder()
                .fieldName("description")
                .errorCode("LENGTH_EXCEEDED")
                .errorMessage("상품 설명은 " + MAX_DESCRIPTION_LENGTH + "자를 초과할 수 없습니다")
                .rejectedValue(description)
                .build());
        }
    }

    private void validatePrice(BigDecimal price, Notification notification) {
        if (price == null) {
            notification.addError(ValidationError.builder()
                .fieldName("price")
                .errorCode("REQUIRED")
                .errorMessage("가격은 필수입니다")
                .rejectedValue(null)
                .build());
        } else if (price.compareTo(BigDecimal.ZERO) <= 0) {
            notification.addError(ValidationError.builder()
                .fieldName("price")
                .errorCode("INVALID_VALUE")
                .errorMessage("가격은 0보다 커야 합니다")
                .rejectedValue(price.toString())
                .build());
        }
    }

    private void validateCategory(Long categoryId, Notification notification) {
        if (categoryId == null) {
            notification.addError(ValidationError.builder()
                .fieldName("categoryId")
                .errorCode("REQUIRED")
                .errorMessage("카테고리는 필수입니다")
                .rejectedValue(null)
                .build());
        }
    }

    public static Notification validateProductCreation(Long categoryId, String sku, String name, 
            BigDecimal price, String description, String shortDescription, String brand, 
            String model, Integer width, Integer height, Integer depth, Integer weight, 
            Integer minStock, Integer maxStock, String taxClass) {
        
        Notification notification = new Notification("Product creation validation");
        
        // Validate required fields
        if (categoryId == null) {
            notification.addError(ValidationError.builder()
                .fieldName("categoryId")
                .errorCode("REQUIRED")
                .errorMessage("카테고리는 필수입니다")
                .rejectedValue(null)
                .build());
        }
        
        if (sku == null || sku.trim().isEmpty()) {
            notification.addError(ValidationError.builder()
                .fieldName("sku")
                .errorCode("REQUIRED")
                .errorMessage("SKU는 필수입니다")
                .rejectedValue(sku)
                .build());
        }
        
        if (name == null || name.trim().isEmpty()) {
            notification.addError(ValidationError.builder()
                .fieldName("name")
                .errorCode("REQUIRED")
                .errorMessage("상품명은 필수입니다")
                .rejectedValue(name)
                .build());
        }
        
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            notification.addError(ValidationError.builder()
                .fieldName("price")
                .errorCode("INVALID_VALUE")
                .errorMessage("가격은 0보다 커야 합니다")
                .rejectedValue(price != null ? price.toString() : null)
                .build());
        }
        
        return notification;
    }

    public static Notification validateProductUpdate(Long productId, String sku, String name, 
            BigDecimal price, String description, String shortDescription, String brand, 
            String model, Integer initialStock, Integer lowStockThreshold, Integer minOrderQuantity,
            Integer maxOrderQuantity, Integer reorderPoint, Integer reorderQuantity, String locationCode) {
        
        Notification notification = new Notification("Product update validation");
        
        // Validate productId is required
        if (productId == null) {
            notification.addError(ValidationError.builder()
                .fieldName("productId")
                .errorCode("REQUIRED")
                .errorMessage("Product ID is required for update.")
                .rejectedValue(null)
                .build());
        }
        
        // Validate SKU if provided
        if (sku != null) {
            if (sku.trim().isEmpty()) {
                notification.addError(ValidationError.builder()
                    .fieldName("sku")
                    .errorCode("INVALID_VALUE")
                    .errorMessage("SKU must not be empty and cannot exceed 100 characters.")
                    .rejectedValue(sku)
                    .build());
            } else if (sku.length() > MAX_SKU_LENGTH) {
                notification.addError(ValidationError.builder()
                    .fieldName("sku")
                    .errorCode("LENGTH_EXCEEDED")
                    .errorMessage("SKU must not be empty and cannot exceed 100 characters.")
                    .rejectedValue(sku)
                    .build());
            }
        }
        
        // Validate name if provided
        if (name != null) {
            if (name.trim().isEmpty()) {
                notification.addError(ValidationError.builder()
                    .fieldName("name")
                    .errorCode("INVALID_VALUE")
                    .errorMessage("Product name must not be empty and cannot exceed 255 characters.")
                    .rejectedValue(name)
                    .build());
            } else if (name.length() > MAX_NAME_LENGTH) {
                notification.addError(ValidationError.builder()
                    .fieldName("name")
                    .errorCode("LENGTH_EXCEEDED")
                    .errorMessage("Product name must not be empty and cannot exceed 255 characters.")
                    .rejectedValue(name)
                    .build());
            }
        }
        
        // Validate description if provided
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            notification.addError(ValidationError.builder()
                .fieldName("description")
                .errorCode("LENGTH_EXCEEDED")
                .errorMessage("Description cannot exceed 2000 characters.")
                .rejectedValue(description)
                .build());
        }
        
        // Validate price if provided
        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                notification.addError(ValidationError.builder()
                    .fieldName("price")
                    .errorCode("INVALID_VALUE")
                    .errorMessage("Price must be greater than 0.")
                    .rejectedValue(price.toString())
                    .build());
            } else if (price.compareTo(new BigDecimal("99999999.99")) > 0) {
                notification.addError(ValidationError.builder()
                    .fieldName("price")
                    .errorCode("INVALID_VALUE")
                    .errorMessage("Price cannot exceed 99,999,999.99.")
                    .rejectedValue(price.toString())
                    .build());
            }
        }
        
        // Validate stock fields if provided
        if (initialStock != null && initialStock < 0) {
            notification.addError(ValidationError.builder()
                .fieldName("initialStock")
                .errorCode("INVALID_VALUE")
                .errorMessage("Stock quantity cannot be negative.")
                .rejectedValue(initialStock.toString())
                .build());
        }
        
        if (minOrderQuantity != null && minOrderQuantity <= 0) {
            notification.addError(ValidationError.builder()
                .fieldName("minOrderQuantity")
                .errorCode("INVALID_VALUE")
                .errorMessage("Minimum order quantity must be greater than 0.")
                .rejectedValue(minOrderQuantity.toString())
                .build());
        }
        
        if (maxOrderQuantity != null && maxOrderQuantity <= 0) {
            notification.addError(ValidationError.builder()
                .fieldName("maxOrderQuantity")
                .errorCode("INVALID_VALUE")
                .errorMessage("Maximum order quantity must be greater than 0.")
                .rejectedValue(maxOrderQuantity.toString())
                .build());
        }
        
        // Validate order quantity range
        if (minOrderQuantity != null && maxOrderQuantity != null && minOrderQuantity > maxOrderQuantity) {
            notification.addError(ValidationError.builder()
                .fieldName("orderQuantityRange")
                .errorCode("INVALID_RANGE")
                .errorMessage("Minimum order quantity cannot be greater than maximum order quantity.")
                .rejectedValue(minOrderQuantity + " > " + maxOrderQuantity)
                .build());
        }
        
        return notification;
    }

    public static Notification validateProductSearch(int page, int size, BigDecimal minPrice, BigDecimal maxPrice) {
        Notification notification = new Notification("Product search validation");
        
        if (page < 0) {
            notification.addError(ValidationError.builder()
                .fieldName("page")
                .errorCode("INVALID_VALUE")
                .errorMessage("페이지 번호는 0 이상이어야 합니다")
                .rejectedValue(String.valueOf(page))
                .build());
        }
        
        if (size <= 0 || size > 100) {
            notification.addError(ValidationError.builder()
                .fieldName("size")
                .errorCode("INVALID_VALUE")
                .errorMessage("페이지 크기는 1 이상 100 이하여야 합니다")
                .rejectedValue(String.valueOf(size))
                .build());
        }
        
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            notification.addError(ValidationError.builder()
                .fieldName("priceRange")
                .errorCode("INVALID_RANGE")
                .errorMessage("최소 가격은 최대 가격보다 작거나 같아야 합니다")
                .rejectedValue(minPrice + " > " + maxPrice)
                .build());
        }
        
        return notification;
    }

}


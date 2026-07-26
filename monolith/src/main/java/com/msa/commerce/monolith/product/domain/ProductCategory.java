package com.msa.commerce.monolith.product.domain;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// product_categories 테이블 기반 도메인 모델. (기존 하드코딩 enum을 대체)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory {

    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private String slug;

    private int displayOrder;

    private boolean active;

    private boolean featured;

    private String imageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ProductCategory reconstitute(Long id, Long parentId, String name, String description,
        String slug, int displayOrder, boolean active, boolean featured, String imageUrl,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
        ProductCategory category = new ProductCategory();
        category.id = id;
        category.parentId = parentId;
        category.name = name;
        category.description = description;
        category.slug = slug;
        category.displayOrder = displayOrder;
        category.active = active;
        category.featured = featured;
        category.imageUrl = imageUrl;
        category.createdAt = createdAt;
        category.updatedAt = updatedAt;
        return category;
    }

    public boolean isRoot() {
        return parentId == null;
    }

}

package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "product_categories",
    indexes = {
        @Index(name = "idx_categories_parent_id", columnList = "parent_id"),
        @Index(name = "idx_categories_slug", columnList = "slug"),
        @Index(name = "idx_categories_active", columnList = "is_active, is_featured")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_categories_parent_name", columnNames = {"parent_id", "name"}),
        @UniqueConstraint(name = "uk_categories_parent_slug", columnNames = {"parent_id", "slug"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategoryJpaEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductCategoryJpaEntity> children = new ArrayList<>();

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100, unique = true, nullable = false)
    private String slug;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = Boolean.FALSE;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ProductCategoryJpaEntity(ProductCategoryJpaEntity parent, String name, String description,
        String slug, Integer displayOrder, Boolean isActive, Boolean isFeatured, String imageUrl) {
        this.parent = parent;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isActive = isActive != null ? isActive : Boolean.TRUE;
        this.isFeatured = isFeatured != null ? isFeatured : Boolean.FALSE;
        this.imageUrl = imageUrl;

        if (parent != null) {
            parent.addChild(this);
        }
    }

    private void addChild(ProductCategoryJpaEntity child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public void updateCategoryInfo(String name, String description, String slug,
        Integer displayOrder, String imageUrl) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (slug != null && !slug.trim().isEmpty()) {
            this.slug = slug;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    public void activate() {
        this.isActive = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }

    public void setAsFeatured() {
        this.isFeatured = Boolean.TRUE;
    }

    public void unsetAsFeatured() {
        this.isFeatured = Boolean.FALSE;
    }

    public void updateDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("표시 순서는 0 이상이어야 합니다.");
        }
        this.displayOrder = displayOrder;
    }

    public void changeParent(ProductCategoryJpaEntity newParent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = newParent;
        if (newParent != null) {
            newParent.addChild(this);
        }
    }

    private void removeChild(ProductCategoryJpaEntity child) {
        if (children != null) {
            children.remove(child);
        }
    }

    public boolean isRootCategory() {
        return parent == null;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean isLeafCategory() {
        return !hasChildren();
    }

    public int getDepth() {
        if (parent == null) {
            return 0;
        }
        return parent.getDepth() + 1;
    }

    public List<ProductCategoryJpaEntity> getAllAncestors() {
        List<ProductCategoryJpaEntity> ancestors = new ArrayList<>();
        ProductCategoryJpaEntity current = this.parent;
        while (current != null) {
            ancestors.add(0, current); // 맨 앞에 추가하여 루트부터 정렬
            current = current.parent;
        }
        return ancestors;
    }

    public String getFullPath() {
        List<ProductCategoryJpaEntity> ancestors = getAllAncestors();
        StringBuilder pathBuilder = new StringBuilder();
        for (ProductCategoryJpaEntity ancestor : ancestors) {
            pathBuilder.append(ancestor.name).append(" > ");
        }
        pathBuilder.append(this.name);
        return pathBuilder.toString();
    }

}

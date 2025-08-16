package com.msa.commerce.monolith.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product 도메인 엔티티
 * 헥사고날 아키텍처의 도메인 레이어
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(length = 500)
    private String imageUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Product(String name, String description, BigDecimal price, 
                   Integer stockQuantity, ProductCategory category, String imageUrl) {
        validateProduct(name, price, stockQuantity, category);
        
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.imageUrl = imageUrl;
        this.status = ProductStatus.ACTIVE;
    }

    /**
     * 도메인 비즈니스 로직: 상품 생성 유효성 검증
     */
    private void validateProduct(String name, BigDecimal price, Integer stockQuantity, ProductCategory category) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        
        if (price.compareTo(new BigDecimal("10000000")) > 0) {
            throw new IllegalArgumentException("가격은 1000만원을 초과할 수 없습니다.");
        }
        
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        }
        
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
    }

    /**
     * 도메인 비즈니스 로직: 재고 차감
     */
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }
        
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        
        this.stockQuantity -= quantity;
    }

    /**
     * 도메인 비즈니스 로직: 재고 증가
     */
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("추가할 수량은 0보다 커야 합니다.");
        }
        
        this.stockQuantity += quantity;
    }

    /**
     * 도메인 비즈니스 로직: 상품 비활성화
     */
    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    /**
     * 도메인 비즈니스 로직: 상품 활성화
     */
    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }
}
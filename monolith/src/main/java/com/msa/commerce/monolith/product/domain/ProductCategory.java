package com.msa.commerce.monolith.product.domain;

import lombok.Getter;

/**
 * 상품 카테고리 열거형
 */
@Getter
public enum ProductCategory {
    
    ELECTRONICS("전자제품", "ELEC"),
    CLOTHING("의류", "CLOT"),
    BOOKS("도서", "BOOK"),
    HOME_GARDEN("홈&가든", "HOME"),
    SPORTS("스포츠", "SPOR"),
    BEAUTY("뷰티", "BEAU"),
    FOOD("식품", "FOOD"),
    TOYS("장난감", "TOYS"),
    AUTOMOTIVE("자동차", "AUTO"),
    HEALTH("건강", "HEAL");

    private final String displayName;
    private final String code;

    ProductCategory(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    /**
     * 코드로 카테고리 찾기
     */
    public static ProductCategory fromCode(String code) {
        for (ProductCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 카테고리 코드입니다: " + code);
    }

    /**
     * 이름으로 카테고리 찾기
     */
    public static ProductCategory fromDisplayName(String displayName) {
        for (ProductCategory category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 카테고리명입니다: " + displayName);
    }
}
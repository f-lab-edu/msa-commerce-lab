package com.msa.commerce.monolith.product.domain;

import lombok.Getter;

@Getter
public enum ProductCategory {

    ELECTRONICS(1L, "전자제품", "ELEC"),
    CLOTHING(2L, "의류", "CLOT"),
    BOOKS(3L, "도서", "BOOK"),
    HOME_GARDEN(4L, "홈&가든", "HOME"),
    SPORTS(5L, "스포츠", "SPOR"),
    BEAUTY(6L, "뷰티", "BEAU"),
    FOOD(7L, "식품", "FOOD"),
    TOYS(8L, "장난감", "TOYS"),
    AUTOMOTIVE(9L, "자동차", "AUTO"),
    HEALTH(10L, "건강", "HEAL");

    private final Long id;

    private final String displayName;

    private final String code;

    ProductCategory(Long id, String displayName, String code) {
        this.id = id;
        this.displayName = displayName;
        this.code = code;
    }

    public static ProductCategory fromId(Long id) {
        for (ProductCategory category : values()) {
            if (category.id.equals(id)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category id: " + id);
    }

}


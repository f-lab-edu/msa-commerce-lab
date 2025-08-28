package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
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

    public static class ProductSearchCommandBuilder {

        private Integer page = 0;

        private Integer size = 20;

        private String sortBy = "createdAt";

        private String sortDirection = "DESC";

        public ProductSearchCommandBuilder page(Integer page) {
            this.page = page != null && page >= 0 ? page : 0;
            return this;
        }

        public ProductSearchCommandBuilder size(Integer size) {
            this.size = size != null && size > 0 && size <= 100 ? size : 20;
            return this;
        }

        public ProductSearchCommandBuilder sortBy(String sortBy) {
            this.sortBy = isValidSortField(sortBy) ? sortBy : "createdAt";
            return this;
        }

        public ProductSearchCommandBuilder sortDirection(String sortDirection) {
            this.sortDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
            return this;
        }

        public ProductSearchCommandBuilder minPrice(BigDecimal minPrice) {
            this.minPrice = minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0 ? minPrice : null;
            return this;
        }

        public ProductSearchCommandBuilder maxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0 ? maxPrice : null;
            return this;
        }

        private boolean isValidSortField(String sortBy) {
            if (sortBy == null)
                return false;
            return sortBy.equals("createdAt") ||
                sortBy.equals("updatedAt") ||
                sortBy.equals("name") ||
                sortBy.equals("price");
        }

    }

    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }

    public boolean isPriceRangeValid() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }

    public boolean hasCategory() {
        return categoryId != null;
    }

    public boolean hasStatus() {
        return status != null;
    }

    public void validate() {
        if (!isPriceRangeValid()) {
            throw new IllegalArgumentException("최소 가격은 최대 가격보다 작거나 같아야 합니다");
        }
        
        if (page != null && page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        
        if (size != null && (size <= 0 || size > 100)) {
            throw new IllegalArgumentException("페이지 크기는 1 이상 100 이하여야 합니다");
        }
    }

}

package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;
import java.util.List;

import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVerifyResponse {

    private boolean allAvailable;

    private List<ProductVerifyResult> results;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductVerifyResult {

        private Long productId;

        private String sku;

        private String name;

        private boolean available;

        private ProductStatus status;

        private Integer requestedQuantity;

        private Integer availableStock;

        private BigDecimal currentPrice;

        private BigDecimal originalPrice;

        private boolean priceChanged;

        private String unavailableReason;

        private Integer minOrderQuantity;

        private Integer maxOrderQuantity;

    }

}

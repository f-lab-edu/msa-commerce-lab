package com.msa.commerce.orchestrator.adapter.out.product;

import java.math.BigDecimal;

import com.msa.commerce.orchestrator.domain.vo.ProductInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long productId;
    private String productName;
    private String sku;
    private String imageUrl;
    private String description;
    private BigDecimal price;

    public ProductInfo toProductInfo() {
        return new ProductInfo(
            productId,
            productName,
            sku,
            imageUrl,
            description,
            price
        );
    }
}

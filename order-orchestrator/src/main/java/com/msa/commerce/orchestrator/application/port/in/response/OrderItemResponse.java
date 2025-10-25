package com.msa.commerce.orchestrator.application.port.in.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItemResponse {

    @JsonProperty("orderItemId")
    private UUID orderItemId;

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("productSku")
    private String productSku;

    @JsonProperty("productVariantId")
    private Long productVariantId;

    @JsonProperty("variantName")
    private String variantName;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("unitPrice")
    private BigDecimal unitPrice;

    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;

}

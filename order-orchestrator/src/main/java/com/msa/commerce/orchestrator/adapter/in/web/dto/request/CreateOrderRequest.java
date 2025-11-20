package com.msa.commerce.orchestrator.adapter.in.web.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Shipping address is required")
    private Map<String, Object> shippingAddress;

    private String sourceChannel;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> orderItems;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotBlank(message = "Product name is required")
        private String productName;

        @NotBlank(message = "Product SKU is required")
        private String productSku;

        private Long productVariantId;

        private String variantName;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;

    }

}

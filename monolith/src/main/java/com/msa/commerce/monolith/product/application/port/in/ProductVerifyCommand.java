package com.msa.commerce.monolith.product.application.port.in;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVerifyCommand {
    
    @NotEmpty(message = "Product verification items cannot be empty.")
    @Valid
    private List<ProductVerifyItem> items;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductVerifyItem {
        
        @NotNull(message = "Product ID is required.")
        @Positive(message = "Product ID must be positive.")
        private Long productId;
        
        @NotNull(message = "Quantity is required.")
        @Positive(message = "Quantity must be positive.")
        private Integer quantity;
    }
}
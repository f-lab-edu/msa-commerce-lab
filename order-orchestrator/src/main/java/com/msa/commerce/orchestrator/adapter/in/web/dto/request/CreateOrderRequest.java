package com.msa.commerce.orchestrator.adapter.in.web.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CreateOrderRequest(
    @NotBlank(message = "주문 번호는 필수입니다.")
    String orderNumber,

    @NotNull(message = "고객 ID는 필수입니다.")
    @Positive(message = "고객 ID는 양수여야 합니다.")
    Long customerId,

    @NotNull(message = "배송지 정보는 필수입니다.")
    Map<String, Object> shippingAddress,

    String sourceChannel,

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> orderItems
) {

    @Builder
    public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 양수여야 합니다.")
        Long productId,

        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @NotBlank(message = "상품 SKU는 필수입니다.")
        String productSku,

        Long productVariantId,

        String variantName,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 양수여야 합니다.")
        Integer quantity,

        @NotNull(message = "단가는 필수입니다.")
        @Positive(message = "단가는 양수여야 합니다.")
        BigDecimal unitPrice
    ) {

    }

}

package com.msa.commerce.orchestrator.application.port.in;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CreateOrderCommand {

    private String orderNumber;

    private Long customerId;

    private Map<String, Object> shippingAddress;

    private String sourceChannel;

    private List<OrderItemCommand> orderItems;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class OrderItemCommand {

        private Long productId;

        private Long productVariantId;

        private String variantName;

        private Integer quantity;

    }

}

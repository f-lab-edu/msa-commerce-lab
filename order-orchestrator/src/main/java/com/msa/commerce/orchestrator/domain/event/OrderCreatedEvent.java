package com.msa.commerce.orchestrator.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderCreatedEvent implements DomainEvent {

    @JsonProperty("metadata")
    private EventMetadata metadata;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("orderNumber")
    private String orderNumber;

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("shippingAddress")
    private Map<String, Object> shippingAddress;

    @JsonProperty("orderItems")
    private List<OrderItemData> orderItems;

    @JsonProperty("sourceChannel")
    private String sourceChannel;

    @JsonProperty("orderDate")
    private LocalDateTime orderDate;

    @Override
    public String getAggregateId() {
        return orderId.toString();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class OrderItemData {

        @JsonProperty("productId")
        private Long productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("unitPrice")
        private BigDecimal unitPrice;

        @JsonProperty("totalPrice")
        private BigDecimal totalPrice;

    }

}

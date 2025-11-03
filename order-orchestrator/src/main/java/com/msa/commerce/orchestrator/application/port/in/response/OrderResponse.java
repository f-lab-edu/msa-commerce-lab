package com.msa.commerce.orchestrator.application.port.in.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderResponse {

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("orderNumber")
    private String orderNumber;

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("status")
    private OrderStatus status;

    @JsonProperty("subtotalAmount")
    private BigDecimal subtotalAmount;

    @JsonProperty("taxAmount")
    private BigDecimal taxAmount;

    @JsonProperty("shippingAmount")
    private BigDecimal shippingAmount;

    @JsonProperty("discountAmount")
    private BigDecimal discountAmount;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("shippingAddress")
    private Map<String, Object> shippingAddress;

    @JsonProperty("orderDate")
    private LocalDateTime orderDate;

    @JsonProperty("confirmedAt")
    private LocalDateTime confirmedAt;

    @JsonProperty("paymentCompletedAt")
    private LocalDateTime paymentCompletedAt;

    @JsonProperty("shippedAt")
    private LocalDateTime shippedAt;

    @JsonProperty("deliveredAt")
    private LocalDateTime deliveredAt;

    @JsonProperty("cancelledAt")
    private LocalDateTime cancelledAt;

    @JsonProperty("sourceChannel")
    private String sourceChannel;

    @JsonProperty("totalItemCount")
    private Integer totalItemCount;

    @JsonProperty("orderItems")
    private List<OrderItemResponse> orderItems;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

}

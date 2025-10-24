package com.msa.commerce.orchestrator.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class OrderSummaryResponse {

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("orderNumber")
    private String orderNumber;

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("status")
    private OrderStatus status;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("totalItemCount")
    private Integer totalItemCount;

    @JsonProperty("orderDate")
    private LocalDateTime orderDate;

    @JsonProperty("sourceChannel")
    private String sourceChannel;

}

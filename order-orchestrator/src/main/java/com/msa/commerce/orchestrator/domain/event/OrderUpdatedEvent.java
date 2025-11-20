package com.msa.commerce.orchestrator.domain.event;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderUpdatedEvent implements DomainEvent {

    @JsonProperty("metadata")
    private EventMetadata metadata;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("orderNumber")
    private String orderNumber;

    @JsonProperty("previousStatus")
    private OrderStatus previousStatus;

    @JsonProperty("currentStatus")
    private OrderStatus currentStatus;

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("statusChangedAt")
    private LocalDateTime statusChangedAt;

    @JsonProperty("reason")
    private String reason;

    @Override
    public String getAggregateId() {
        return orderId.toString();
    }

}

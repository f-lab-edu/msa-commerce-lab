package com.msa.commerce.orchestrator.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class PaymentResultEvent implements DomainEvent {

    @JsonProperty("metadata")
    private EventMetadata metadata;

    @JsonProperty("paymentId")
    private UUID paymentId;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("paymentStatus")
    private PaymentStatus paymentStatus;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("paymentMethod")
    private String paymentMethod;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("processedAt")
    private LocalDateTime processedAt;

    @JsonProperty("failureReason")
    private String failureReason;

    @Override
    public String getAggregateId() {
        return orderId.toString();
    }

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING,
        CANCELLED
    }

}

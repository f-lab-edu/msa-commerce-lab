package com.msa.commerce.materializedview.adapter.in.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// order-orchestrator가 발행하는 order.created 이벤트의 소비자 측 계약.
// 뷰 갱신에 불필요한 필드(shippingAddress 등)는 수신하지 않는다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
    EventMetadata metadata,
    UUID orderId,
    String orderNumber,
    Long customerId,
    BigDecimal totalAmount,
    String currency,
    List<OrderItemData> orderItems,
    String sourceChannel,
    LocalDateTime orderDate
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItemData(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
    ) {

    }

}

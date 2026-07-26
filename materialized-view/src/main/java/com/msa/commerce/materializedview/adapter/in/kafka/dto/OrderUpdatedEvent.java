package com.msa.commerce.materializedview.adapter.in.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 상태는 발행 측 enum이 추가되어도 역직렬화가 깨지지 않도록 String으로 수신한다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderUpdatedEvent(
    EventMetadata metadata,
    UUID orderId,
    String orderNumber,
    String previousStatus,
    String currentStatus,
    Long customerId,
    LocalDateTime statusChangedAt,
    String reason
) {

}

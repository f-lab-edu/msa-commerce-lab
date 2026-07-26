package com.msa.commerce.materializedview.adapter.in.kafka.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventMetadata(
    String eventId,
    String correlationId,
    LocalDateTime timestamp,
    String eventType,
    String source,
    Integer version
) {

}

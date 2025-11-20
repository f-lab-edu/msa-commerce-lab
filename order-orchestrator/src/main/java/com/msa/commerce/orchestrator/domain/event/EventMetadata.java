package com.msa.commerce.orchestrator.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventMetadata {

    private String eventId;

    private String correlationId;

    private LocalDateTime timestamp;

    private String eventType;

    private String source;

    private Integer version;

    public static EventMetadata create(String eventType, String source) {
        return EventMetadata.builder()
            .eventId(UUID.randomUUID().toString())
            .correlationId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .eventType(eventType)
            .source(source)
            .version(1)
            .build();
    }

    public static EventMetadata create(String eventType, String source, String correlationId) {
        return EventMetadata.builder()
            .eventId(UUID.randomUUID().toString())
            .correlationId(correlationId)
            .timestamp(LocalDateTime.now())
            .eventType(eventType)
            .source(source)
            .version(1)
            .build();
    }

}

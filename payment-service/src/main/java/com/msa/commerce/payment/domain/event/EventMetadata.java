package com.msa.commerce.payment.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * order-orchestrator 의 EventMetadata 와 필드 이름이 1:1 로 대응해야 한다.
 * 소비자가 USE_TYPE_INFO_HEADERS=false 로 고정 타입 역직렬화를 하기 때문에
 * 클래스는 공유하지 않되 JSON 모양은 정확히 같아야 한다.
 */
public record EventMetadata(
    String eventId,
    String correlationId,
    LocalDateTime timestamp,
    String eventType,
    String source,
    Integer version
) {

    private static final int SCHEMA_VERSION = 1;

    public static EventMetadata create(String eventType, String source, String correlationId) {
        return new EventMetadata(
            UUID.randomUUID().toString(),
            correlationId != null ? correlationId : UUID.randomUUID().toString(),
            LocalDateTime.now(),
            eventType,
            source,
            SCHEMA_VERSION);
    }

}

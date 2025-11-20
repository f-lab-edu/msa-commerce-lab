package com.msa.commerce.orchestrator.domain.event;

import java.time.LocalDateTime;

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
public class FailedEvent<T extends DomainEvent> {

    @JsonProperty("originalTopic")
    private String originalTopic;

    @JsonProperty("originalPartition")
    private Integer originalPartition;

    @JsonProperty("originalOffset")
    private Long originalOffset;

    @JsonProperty("consumerGroup")
    private String consumerGroup;

    @JsonProperty("failedAt")
    private LocalDateTime failedAt;

    @JsonProperty("totalAttempts")
    private Integer totalAttempts;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("errorStackTrace")
    private String errorStackTrace;

    @JsonProperty("errorType")
    private String errorType;

    @JsonProperty("payload")
    private T payload;

    public static <T extends DomainEvent> FailedEvent<T> from(
        RetryableEvent<T> retryableEvent,
        String consumerGroup
    ) {
        return FailedEvent.<T>builder()
            .originalTopic(retryableEvent.getOriginalTopic())
            .originalPartition(retryableEvent.getOriginalPartition())
            .originalOffset(retryableEvent.getOriginalOffset())
            .consumerGroup(consumerGroup)
            .failedAt(LocalDateTime.now())
            .totalAttempts(retryableEvent.getRetryCount())
            .errorMessage(retryableEvent.getErrorMessage())
            .errorStackTrace(retryableEvent.getErrorStackTrace())
            .errorType("MAX_RETRIES_EXCEEDED")
            .payload(retryableEvent.getPayload())
            .build();
    }

    public static <T extends DomainEvent> FailedEvent<T> create(
        String originalTopic,
        Integer originalPartition,
        Long originalOffset,
        String consumerGroup,
        String errorMessage,
        String errorStackTrace,
        String errorType,
        T payload
    ) {
        return FailedEvent.<T>builder()
            .originalTopic(originalTopic)
            .originalPartition(originalPartition)
            .originalOffset(originalOffset)
            .consumerGroup(consumerGroup)
            .failedAt(LocalDateTime.now())
            .totalAttempts(1)
            .errorMessage(errorMessage)
            .errorStackTrace(errorStackTrace)
            .errorType(errorType)
            .payload(payload)
            .build();
    }

}

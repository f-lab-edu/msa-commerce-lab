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
public class RetryableEvent<T extends DomainEvent> {

    @JsonProperty("originalTopic")
    private String originalTopic;

    @JsonProperty("originalPartition")
    private Integer originalPartition;

    @JsonProperty("originalOffset")
    private Long originalOffset;

    @JsonProperty("retryCount")
    private Integer retryCount;

    @JsonProperty("maxRetries")
    private Integer maxRetries;

    @JsonProperty("lastAttemptAt")
    private LocalDateTime lastAttemptAt;

    @JsonProperty("nextRetryAt")
    private LocalDateTime nextRetryAt;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("errorStackTrace")
    private String errorStackTrace;

    @JsonProperty("payload")
    private T payload;

    public static <T extends DomainEvent> RetryableEvent<T> create(
        String originalTopic,
        Integer originalPartition,
        Long originalOffset,
        Integer maxRetries,
        T payload,
        String errorMessage,
        String stackTrace
    ) {
        return RetryableEvent.<T>builder()
            .originalTopic(originalTopic)
            .originalPartition(originalPartition)
            .originalOffset(originalOffset)
            .retryCount(1)
            .maxRetries(maxRetries)
            .lastAttemptAt(LocalDateTime.now())
            .nextRetryAt(LocalDateTime.now().plusSeconds(10))
            .errorMessage(errorMessage)
            .errorStackTrace(stackTrace)
            .payload(payload)
            .build();
    }

    public boolean canRetry() {
        return retryCount < maxRetries && LocalDateTime.now().isAfter(nextRetryAt);
    }

    public RetryableEvent<T> incrementRetry(String errorMessage, String stackTrace) {
        return RetryableEvent.<T>builder()
            .originalTopic(this.originalTopic)
            .originalPartition(this.originalPartition)
            .originalOffset(this.originalOffset)
            .retryCount(this.retryCount + 1)
            .maxRetries(this.maxRetries)
            .lastAttemptAt(LocalDateTime.now())
            .nextRetryAt(calculateNextRetryTime(this.retryCount + 1))
            .errorMessage(errorMessage)
            .errorStackTrace(stackTrace)
            .payload(this.payload)
            .build();
    }

    private LocalDateTime calculateNextRetryTime(int retryCount) {
        long delaySeconds = (long)Math.pow(2, retryCount) * 10;
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

}

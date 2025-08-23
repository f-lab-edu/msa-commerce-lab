package com.msa.commerce.common.monitoring;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Alert {

    private final String type;

    private final String endpoint;

    private final String message;

    private final String severity;

    private final LocalDateTime timestamp;

}

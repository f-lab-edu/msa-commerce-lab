package com.msa.commerce.orchestrator.application.port.in;

import java.time.LocalDateTime;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSearchCriteria {

    private final Long customerId;

    private final OrderStatus status;

    private final LocalDateTime startDate;

    private final LocalDateTime endDate;

    @Builder.Default
    private final Integer page = 0;

    @Builder.Default
    private final Integer size = 10;

    @Builder.Default
    private final String sort = "orderDate,desc";

}

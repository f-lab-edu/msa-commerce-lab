package com.msa.commerce.orchestrator.adapter.in.web.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSearchParams {

    private Long customerId;

    private OrderStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sort = "orderDate,desc";

    public void validateAndSetDefaults() {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0 || size > 100) {
            size = 10;
        }
        if (sort == null || sort.trim().isEmpty()) {
            sort = "orderDate,desc";
        }
    }

}

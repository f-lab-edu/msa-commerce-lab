package com.msa.commerce.monolith.product.domain.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class ProductDeletedEvent {

    private final Long productId;

    private final String sku;

    private final String productName;

    private final LocalDateTime deletedAt;

}

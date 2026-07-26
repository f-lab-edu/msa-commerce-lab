package com.msa.commerce.monolith.product.application.port.in;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryChangeCommand {

    private final Long productId;

    private final Long variantId;

    private final String locationCode;

    private final int quantity;

    private final String reason;

    private final String referenceType;

    private final String referenceId;

}

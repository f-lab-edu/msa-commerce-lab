package com.msa.commerce.monolith.product.application.port.in;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductPageResponse {

    private final List<ProductSearchResponse> content;

    private final int page;

    private final int size;

    private final long totalElements;

    private final int totalPages;

    private final boolean first;

    private final boolean last;

    private final boolean hasNext;

    private final boolean hasPrevious;

}

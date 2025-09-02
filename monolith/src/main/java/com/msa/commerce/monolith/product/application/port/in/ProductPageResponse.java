package com.msa.commerce.monolith.product.application.port.in;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.msa.commerce.monolith.product.domain.Product;

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

    public static ProductPageResponse from(Page<Product> productPage,
        Function<Product, ProductSearchResponse> mapper) {
        List<ProductSearchResponse> content = productPage.getContent()
            .stream()
            .map(mapper)
            .collect(Collectors.toList());

        return ProductPageResponse.builder()
            .content(content)
            .page(productPage.getNumber())
            .size(productPage.getSize())
            .totalElements(productPage.getTotalElements())
            .totalPages(productPage.getTotalPages())
            .first(productPage.isFirst())
            .last(productPage.isLast())
            .hasNext(productPage.hasNext())
            .hasPrevious(productPage.hasPrevious())
            .build();
    }

}

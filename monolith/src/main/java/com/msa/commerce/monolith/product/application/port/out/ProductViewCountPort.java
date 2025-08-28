package com.msa.commerce.monolith.product.application.port.out;

public interface ProductViewCountPort {

    void incrementViewCount(Long productId);

    Long getViewCount(Long productId);

}


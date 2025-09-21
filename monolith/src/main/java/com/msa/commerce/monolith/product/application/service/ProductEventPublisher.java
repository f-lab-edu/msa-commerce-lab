package com.msa.commerce.monolith.product.application.service;

import com.msa.commerce.monolith.product.domain.Product;

public interface ProductEventPublisher {

    void publishProductDeletedEvent(Product product);

}

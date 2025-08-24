package com.msa.commerce.monolith.product.application.port.in;

public interface ProductSearchUseCase {

    ProductPageResponse searchProducts(ProductSearchCommand command);
}
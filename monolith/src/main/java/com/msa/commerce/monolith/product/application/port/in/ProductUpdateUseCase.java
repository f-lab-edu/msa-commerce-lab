package com.msa.commerce.monolith.product.application.port.in;

public interface ProductUpdateUseCase {

    ProductResponse updateProduct(ProductUpdateCommand command);

}


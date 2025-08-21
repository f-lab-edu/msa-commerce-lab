package com.msa.commerce.monolith.product.application.port.in;

public interface ProductCreateUseCase {

    ProductResponse createProduct(ProductCreateCommand command);

}

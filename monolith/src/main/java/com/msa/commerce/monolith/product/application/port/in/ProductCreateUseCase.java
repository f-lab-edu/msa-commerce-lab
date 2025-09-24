package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.application.port.in.command.ProductCreateCommand;

public interface ProductCreateUseCase {

    ProductResponse createProduct(ProductCreateCommand command);

}

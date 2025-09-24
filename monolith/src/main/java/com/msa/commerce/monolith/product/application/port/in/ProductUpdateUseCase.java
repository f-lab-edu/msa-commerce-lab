package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.application.port.in.command.ProductUpdateCommand;

public interface ProductUpdateUseCase {

    ProductResponse updateProduct(ProductUpdateCommand command);

}

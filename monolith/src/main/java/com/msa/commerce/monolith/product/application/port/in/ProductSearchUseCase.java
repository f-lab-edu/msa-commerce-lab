package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.application.port.in.command.ProductSearchCommand;

public interface ProductSearchUseCase {

    ProductPageResponse searchProducts(ProductSearchCommand command);

}

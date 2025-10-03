package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.application.port.in.command.ProductSearchCommand;

public interface ProductGetUseCase {

    ProductResponse getProduct(Long productId);

    ProductPageResponse searchProducts(ProductSearchCommand searchCommand);

}

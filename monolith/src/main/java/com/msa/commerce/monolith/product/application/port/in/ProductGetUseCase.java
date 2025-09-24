package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.application.port.in.command.ProductSearchCommand;

public interface ProductGetUseCase {

    ProductResponse getProduct(Long productId);

    ProductResponse getProduct(Long productId, boolean increaseViewCount);

    ProductPageResponse searchProducts(ProductSearchCommand searchCommand);

}

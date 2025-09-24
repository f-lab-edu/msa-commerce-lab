package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.application.port.in.command.ProductVerifyCommand;

public interface ProductVerifyUseCase {

    ProductVerifyResponse verifyProducts(ProductVerifyCommand command);

}


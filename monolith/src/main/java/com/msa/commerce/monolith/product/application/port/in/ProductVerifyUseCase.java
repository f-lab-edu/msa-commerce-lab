package com.msa.commerce.monolith.product.application.port.in;

public interface ProductVerifyUseCase {

    ProductVerifyResponse verifyProducts(ProductVerifyCommand command);

}

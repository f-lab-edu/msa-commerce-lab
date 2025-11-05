package com.msa.commerce.orchestrator.adapter.out.product;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.InternalException;

/**
 * Product Service에서 상품을 찾을 수 없을 때 발생하는 예외
 */
public class ProductNotFoundException extends InternalException {

    public ProductNotFoundException(String message) {
        super(message, ErrorCode.PRODUCT_NOT_FOUND.getCode());
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, ErrorCode.PRODUCT_NOT_FOUND.getCode(), cause);
    }
}

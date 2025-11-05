package com.msa.commerce.orchestrator.adapter.out.product;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.InternalException;

/**
 * Product Service 호출 실패 시 발생하는 예외
 */
public class ProductServiceException extends InternalException {

    public ProductServiceException(String message) {
        super(message, ErrorCode.EXTERNAL_PRODUCT_SERVICE_ERROR.getCode());
    }

    public ProductServiceException(String message, Throwable cause) {
        super(message, ErrorCode.EXTERNAL_PRODUCT_SERVICE_ERROR.getCode(), cause);
    }
}

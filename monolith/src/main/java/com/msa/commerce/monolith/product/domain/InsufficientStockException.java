package com.msa.commerce.monolith.product.domain;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ValidationException;

public class InsufficientStockException extends ValidationException {

    public InsufficientStockException(String message) {
        super(message, ErrorCode.INSUFFICIENT_STOCK.getCode());
    }

}

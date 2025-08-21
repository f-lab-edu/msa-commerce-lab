package com.msa.commerce.common.exception;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message, String errorCode) {
        super(message, errorCode);
    }

}

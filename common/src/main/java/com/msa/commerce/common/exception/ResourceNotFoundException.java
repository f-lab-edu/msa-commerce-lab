package com.msa.commerce.common.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }

}

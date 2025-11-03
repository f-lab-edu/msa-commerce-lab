package com.msa.commerce.common.exception;

public class ResourceNotFoundException extends InternalException {

    public ResourceNotFoundException(String message) {
        this(message, null);
    }

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }

}

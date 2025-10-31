package com.msa.commerce.common.exception;

public class DuplicateResourceException extends InternalException {

    public DuplicateResourceException(String message, String errorCode) {
        super(message, errorCode);
    }

}

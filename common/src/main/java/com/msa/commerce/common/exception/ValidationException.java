package com.msa.commerce.common.exception;

public class ValidationException extends InternalException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

}

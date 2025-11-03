package com.msa.commerce.common.exception;

import lombok.Getter;

@Getter
public abstract class InternalException extends RuntimeException {

    private final String errorCode;

    protected InternalException(String message) {
        super(message);
        this.errorCode = null;
    }

    protected InternalException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected InternalException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    protected InternalException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}

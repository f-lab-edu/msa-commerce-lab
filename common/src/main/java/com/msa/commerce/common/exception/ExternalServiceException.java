package com.msa.commerce.common.exception;

public class ExternalServiceException extends InternalException {

    public ExternalServiceException(String message) {
        super(message, ErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
    }

    public ExternalServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ExternalServiceException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }

}

package com.msa.commerce.common.exception;

public class NoChangesProvidedException extends BusinessException {

    public NoChangesProvidedException(String message, String errorCode) {
        super(message, errorCode);
    }

    public NoChangesProvidedException(String message) {
        super(message, ErrorCode.PRODUCT_NO_CHANGES_PROVIDED.getCode());
    }

    public static NoChangesProvidedException forProductUpdate() {
        return new NoChangesProvidedException("No fields to update provided.");
    }

}

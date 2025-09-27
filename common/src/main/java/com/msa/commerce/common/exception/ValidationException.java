package com.msa.commerce.common.exception;

public class ValidationException extends RuntimeException {

	private final String errorCode;

	public ValidationException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public ValidationException(String message, String errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}

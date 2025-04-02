package com.sullung2yo.seatcatcher.config.exception;

public class TokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public TokenException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

package com.sullung2yo.seatcatcher.config.exception;

public class UserException extends RuntimeException{

    private final ErrorCode errorCode;

    public UserException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

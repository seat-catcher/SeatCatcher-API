package com.sullung2yo.seatcatcher.config.exception;

public class SubwayLineNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public SubwayLineNotFoundException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

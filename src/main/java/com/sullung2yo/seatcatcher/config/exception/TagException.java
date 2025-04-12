package com.sullung2yo.seatcatcher.config.exception;

public class TagException extends RuntimeException {

    private final ErrorCode errorCode;

    public TagException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}

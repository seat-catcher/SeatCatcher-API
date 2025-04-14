package com.sullung2yo.seatcatcher.common.exception;

public class SubwayStationException extends RuntimeException {

    private final ErrorCode errorCode;

    public SubwayStationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

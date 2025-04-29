package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class SeatException extends RuntimeException {
    private final ErrorCode errorCode;

    public SeatException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

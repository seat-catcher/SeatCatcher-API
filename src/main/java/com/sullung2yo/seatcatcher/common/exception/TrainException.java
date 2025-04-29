package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class TrainException extends RuntimeException {
    private final ErrorCode errorCode;

    public TrainException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

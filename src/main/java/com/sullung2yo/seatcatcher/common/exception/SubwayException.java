package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class SubwayException extends RuntimeException {

    private final ErrorCode errorCode;

    public SubwayException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}

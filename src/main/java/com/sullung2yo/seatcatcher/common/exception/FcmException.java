package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class FcmException extends RuntimeException{
    private final ErrorCode errorCode;

    public FcmException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

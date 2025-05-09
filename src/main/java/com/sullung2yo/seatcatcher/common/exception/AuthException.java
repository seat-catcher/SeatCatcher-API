package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

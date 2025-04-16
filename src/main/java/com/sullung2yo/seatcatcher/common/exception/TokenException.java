package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class TokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public TokenException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}

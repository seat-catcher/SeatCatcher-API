package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class TagException extends RuntimeException {

    private final ErrorCode errorCode;

    public TagException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}

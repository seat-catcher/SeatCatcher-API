package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;

@Getter
public class PathHistoryException extends RuntimeException {
    private final ErrorCode errorCode;

    public PathHistoryException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

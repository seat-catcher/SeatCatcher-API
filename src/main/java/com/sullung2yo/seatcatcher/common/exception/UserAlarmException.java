package com.sullung2yo.seatcatcher.common.exception;


import lombok.Getter;

@Getter
public class UserAlarmException extends RuntimeException{

    private final ErrorCode errorCode;

    public UserAlarmException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}

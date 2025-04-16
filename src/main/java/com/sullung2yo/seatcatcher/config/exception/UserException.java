package com.sullung2yo.seatcatcher.config.exception;


import lombok.Getter;

@Getter
public class UserException extends RuntimeException{

    private final ErrorCode errorCode;

    public UserException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}

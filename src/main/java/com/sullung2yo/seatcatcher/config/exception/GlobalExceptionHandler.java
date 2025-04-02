package com.sullung2yo.seatcatcher.config.exception;

import com.sullung2yo.seatcatcher.config.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        /*
          400 Bad Request 처리 메서드
         */
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error("Bad Request")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        /*
          405 Method Not Allowed 처리 메서드
         */
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error("Method Not Allowed")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenException(TokenException ex) {
        /*
            401 Unauthorized, 404 Not found 처리 메서드 (토큰 관련 ExceptionHandler)
         */
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error(String.valueOf(ex.getErrorCode().getHttpStatus()))
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        /*
          500 Internal Server Error 처리 메서드
         */
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error("Internal Server Error")
                        .message(ex.getMessage())
                        .build());
    }
}

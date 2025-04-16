package com.sullung2yo.seatcatcher.common.exception;

import com.sullung2yo.seatcatcher.common.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        if (ex.getErrorCode() == ErrorCode.INVALID_TOKEN) {
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Token", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.TOKEN_NOT_FOUND){
            return createErrorResponse(HttpStatus.NOT_FOUND, "Token Not Found", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.EXPIRED_TOKEN) {
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "Expired Token", ex.getMessage());
        }
        else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Token Internal Server Error", ex.getMessage());
        }
    }

    @ExceptionHandler(TagException.class)
    public ResponseEntity<ErrorResponse> handleTagException(TagException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Tag Not Found", ex.getMessage());
    }

    @ExceptionHandler(SubwayException.class)
    public ResponseEntity<ErrorResponse> handleSubwayException(SubwayException ex) {
        if (ex.getErrorCode() == ErrorCode.SUBWAY_NOT_FOUND) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "Subway Not Found", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.SUBWAY_LINE_NOT_FOUND) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "Subway Line Not Found", ex.getMessage());
        }
        else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Subway Internal Server Error", ex.getMessage());
        }
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex) {
        if (ex.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.INVALID_PROFILE_IMAGE_NUM) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Profile Image Number", ex.getMessage());
        }
        else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "User Internal Server Error", ex.getMessage());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        /*
          500 Internal Server Error 처리 메서드
         */
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String errorTitle, String errorMessage) {
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error(errorTitle)
                        .message(errorMessage)
                        .build());
    }
}

package com.sullung2yo.seatcatcher.common.exception;

import com.sullung2yo.seatcatcher.common.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        /*
          400 Bad Request 처리 메서드
         */
        log.error("HttpMessageNotReadableException", ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        /*
          405 Method Not Allowed 처리 메서드
         */
        log.error("HttpRequestMethodNotSupportedException", ex);
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage());
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
        /*
          404 Not Found 처리 메서드 (태그 관련 ExceptionHandler)
         */
        return createErrorResponse(HttpStatus.NOT_FOUND, "Tag Not Found", ex.getMessage());
    }

    @ExceptionHandler(SubwayException.class)
    public ResponseEntity<ErrorResponse> handleSubwayException(SubwayException ex) {
        /*
          지하철 관련 ExceptionHandler
         */
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
        /*
          유저 관련 ExceptionHandler
         */
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

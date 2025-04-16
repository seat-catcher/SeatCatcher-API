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
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error(String.valueOf(ex.getErrorCode().getHttpStatus()))
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(TagException.class)
    public ResponseEntity<?> handleTagNotFoundException(TagException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    ErrorResponse.builder()
                            .error("Tag Not Found")
                            .message(ex.getMessage())
                            .build()
                );
    }

    @ExceptionHandler(SubwayException.class)
    public ResponseEntity<?> handleSubwayException(SubwayException ex) {
        if (ex.getErrorCode() == ErrorCode.SUBWAY_NOT_FOUND) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            ErrorResponse.builder()
                                    .error("Subway Not Found")
                                    .message(ex.getMessage())
                                    .build()
                    );
        }
        else if (ex.getErrorCode() == ErrorCode.SUBWAY_LINE_NOT_FOUND) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            ErrorResponse.builder()
                                    .error("Subway Line Not Found")
                                    .message(ex.getMessage())
                                    .build()
                    );
        }
        else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            ErrorResponse.builder()
                                    .error("Subway Internal Server Error")
                                    .message(ex.getMessage())
                                    .build()
                    );
        }
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> handleUserException(UserException ex) {
        if (ex.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            ErrorResponse.builder()
                                    .error("User Not Found")
                                    .message(ex.getMessage())
                                    .build()
                    );
        }
        else if (ex.getErrorCode() == ErrorCode.INVALID_PROFILE_IMAGE_NUM) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            ErrorResponse.builder()
                                    .error("Invalid Profile Image Number")
                                    .message(ex.getMessage())
                                    .build()
                    );
        }
        else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            ErrorResponse.builder()
                                    .error("User Internal Server Error")
                                    .message(ex.getMessage())
                                    .build()
                    );
        }
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

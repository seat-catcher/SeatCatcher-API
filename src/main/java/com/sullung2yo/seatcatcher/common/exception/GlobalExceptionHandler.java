package com.sullung2yo.seatcatcher.common.exception;

import com.sullung2yo.seatcatcher.common.exception.dto.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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
        log.error("TokenException", ex);
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
        log.error("TagException", ex);
        return createErrorResponse(HttpStatus.NOT_FOUND, "Tag Not Found", ex.getMessage());
    }

    @ExceptionHandler(SubwayException.class)
    public ResponseEntity<ErrorResponse> handleSubwayException(SubwayException ex) {
        /*
          지하철 관련 ExceptionHandler
         */
        log.error("SubwayException", ex);
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
        log.error("UserException", ex);
        if (ex.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.INVALID_PROFILE_IMAGE_NUM) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Profile Image Number", ex.getMessage());
        }
        else if(ex.getErrorCode() == ErrorCode.INSUFFICIENT_CREDIT){
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Insufficient Credit", ex.getMessage());
        }
        else if(ex.getErrorCode() == ErrorCode.USER_STATUS_NOT_FOUND){
            return createErrorResponse(HttpStatus.NOT_FOUND, "User Status Not Found", ex.getMessage());
        }
        else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "User Internal Server Error", ex.getMessage());
        }
    }

    @ExceptionHandler(SeatException.class)
    public ResponseEntity<ErrorResponse> handleSeatException(SeatException ex, HttpServletRequest request) {
        /*
          좌석 관련 ExceptionHandler
         */
        log.error("SeatException 발생 - URI : {}, Message: {}", request.getRequestURI(), ex.getMessage(), ex);
        if (ex.getErrorCode() == ErrorCode.SEAT_ALREADY_RESERVED) {
            return createErrorResponse(HttpStatus.CONFLICT, "Seat already reserved", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.SEAT_NOT_FOUND) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "Seat not found", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.USER_ALREADY_RESERVED) {
            return createErrorResponse(HttpStatus.CONFLICT, "User already reserved", ex.getMessage());
        }
        else if (ex.getErrorCode() == ErrorCode.USER_NOT_RESERVED) {
            return createErrorResponse(HttpStatus.CONFLICT, "User not reserved", ex.getMessage());
        }
        else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
        }
    }

    @ExceptionHandler(TrainException.class)
    public ResponseEntity<ErrorResponse> handleTrainException(TrainException ex) {
        /*
          기차 관련 ExceptionHandler
         */
        log.error("TrainException", ex);
        if (ex.getErrorCode() == ErrorCode.TRAIN_NOT_FOUND) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "TrainSeatGroup Not Found", ex.getMessage());
        }
        else {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "TrainSeatGroup Internal Server Error", ex.getMessage());
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        /*
          404 Not Found 처리 메서드
         */
        log.error("EntityNotFoundException", ex);
        return createErrorResponse(HttpStatus.NOT_FOUND, "Entity Not Found", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        String errorMessage = "Validation failed: " + errors.toString();
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        /*
          500 Internal Server Error 처리 메서드
         */
        log.error("Exception", ex);
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

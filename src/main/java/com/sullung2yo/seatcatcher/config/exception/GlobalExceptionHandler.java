package com.sullung2yo.seatcatcher.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 요청 본문을 읽을 수 없을 경우 발생하는 HttpMessageNotReadableException 예외를 처리하여
     * 400 Bad Request 응답을 반환합니다.
     *
     * <p>본 메서드는 요청 본문의 파싱 실패 시, "Bad Request" 메시지와 예외의 상세 메시지를 포함하는 응답 본문을 생성하고,
     * 이를 400 상태 코드와 함께 반환합니다.</p>
     *
     * @param ex 요청 본문을 읽을 수 없어 발생한 예외
     * @return 에러 메시지와 400 Bad Request 상태를 담은 ResponseEntity 객체
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        /*
          400 Bad Request 처리 메서드
         */
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * 지원되지 않는 HTTP 요청 메서드를 처리하는 예외 핸들러입니다.
     *
     * 요청된 HTTP 메서드가 지원되지 않을 경우 발생하는 HttpRequestMethodNotSupportedException 예외를 처리하여,
     * 에러 메시지와 예외 세부 내용을 포함한 응답 본문과 함께 405 (Method Not Allowed) 상태 코드를 반환합니다.
     *
     * @param ex 발생한 HttpRequestMethodNotSupportedException 예외
     * @return 405 상태 코드와 에러 정보를 담은 ResponseEntity 객체
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        /*
          405 Method Not Allowed 처리 메서드
         */
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Method Not Allowed");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 전역 예외 처리 메서드로, 발생한 모든 예외를 캡처하여 HTTP 500 응답을 반환합니다.
     * 예외의 메시지를 포함한 응답 바디를 구성하여 클라이언트에게 내부 서버 오류를 알립니다.
     *
     * @param ex 발생한 예외
     * @return 예외 메시지를 포함하는 HTTP 500 상태의 ResponseEntity 객체
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        /*
          500 Internal Server Error 처리 메서드
         */
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

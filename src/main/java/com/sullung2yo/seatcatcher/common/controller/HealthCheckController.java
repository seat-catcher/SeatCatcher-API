package com.sullung2yo.seatcatcher.common.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    /**
     * 애플리케이션의 헬스 체크 상태를 확인하는 엔드포인트입니다.
     * 
     * 이 메서드는 HTTP GET 요청에 대해 HTTP 200 OK 상태와 "ok" 메시지를 포함한 응답을 반환하여,
     * 서버가 정상적으로 동작 중임을 확인하는 간단한 헬스 체크 기능을 제공합니다.
     *
     * @return "ok" 메시지를 포함한 HTTP 200 OK 상태의 ResponseEntity 객체
     */
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ok");
    }
}

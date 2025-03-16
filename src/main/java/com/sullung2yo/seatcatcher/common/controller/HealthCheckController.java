package com.sullung2yo.seatcatcher.common.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "Common", description = "Common APIs (use for health check)")
public class HealthCheckController {

    /**
     * 애플리케이션의 헬스 체크 상태를 확인하는 엔드포인트입니다.
     * 이 메서드는 HTTP GET 요청에 대해 HTTP 200 OK 상태와 "ok" 메시지를 포함한 응답을 반환하여,
     * 서버가 정상적으로 동작 중임을 확인하는 간단한 헬스 체크 기능을 제공합니다.
     *
     * @return "ok" 메시지를 포함한 HTTP 200 OK 상태의 ResponseEntity 객체
     */
    @GetMapping
    @Operation(summary = "Health Check", description = "Check the health status of the application")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<String> healthCheck() throws Exception {
        log.info("Health check request received");
        return ResponseEntity.ok("ok");
    }
}

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

    @GetMapping
    @Operation(summary = "Health Check", description = "Check the health status of the application")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check request received");
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/auth")
    @Operation(summary = "Auth Check", description = "Check if request user is authenticated")
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<String> authCheck() {
        log.info("Auth check request received");
        return ResponseEntity.ok("ok");
    }

}

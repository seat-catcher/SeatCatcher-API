package com.sullung2yo.seatcatcher.common.controller;


import com.sullung2yo.seatcatcher.user.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "Common", description = "Health check API")
public class HealthCheckController {

    @GetMapping
    @Operation(
            summary = "서버 헬스 체크 API",
            description = "단순한 서버 헬스 체크 목적으로 사용하는 API 입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))
                    )
            }
    )
    public ResponseEntity<String> healthCheck() {
        log.info("Health check request received");
        return ResponseEntity.ok("ok");
    }

}

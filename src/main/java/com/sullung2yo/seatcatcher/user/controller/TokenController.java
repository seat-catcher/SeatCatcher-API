package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.request.TokenRefreshRequest;
import com.sullung2yo.seatcatcher.user.dto.response.TokenResponse;
import com.sullung2yo.seatcatcher.user.service.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Tag(name = "Token API", description = "Token APIs")
public class TokenController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token", description = "토큰 갱신 API")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<?> tokenRefresh(@RequestBody TokenRefreshRequest tokenRefreshRequest) throws Exception {
        // Refresh token 재발급
        List<String> tokens = authServiceImpl.refreshToken(tokenRefreshRequest.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(tokens.get(0), tokens.get(1)));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate access token", description = "Access token 유효성 검사 API")
    @ApiResponse(responseCode = "200", description = "OK")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String accessToken) throws Exception {
        // accessToken 검증
        boolean isValid = authServiceImpl.validateAccessToken(accessToken);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Valid");
    }
}

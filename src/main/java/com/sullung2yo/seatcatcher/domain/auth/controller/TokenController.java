package com.sullung2yo.seatcatcher.domain.auth.controller;


import com.sullung2yo.seatcatcher.common.exception.dto.ErrorResponse;
import com.sullung2yo.seatcatcher.domain.auth.dto.request.TokenRefreshRequest;
import com.sullung2yo.seatcatcher.domain.auth.dto.response.TokenResponse;
import com.sullung2yo.seatcatcher.domain.auth.service.AuthService;
//import com.sullung2yo.seatcatcher.domain.auth.service.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

//    private final AuthServiceImpl authServiceImpl;
    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 갱신 API",
            description = "Refresh 토큰을 사용하여 새로운 Access/Refresh 토큰을 반환합니다. 기존 Refersh 토큰은 폐기됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 토큰 재발급",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Refresh 토큰이 만료되었거나 유효하지 않음",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "토큰에 담긴 사용자 또는 Refresh 토큰이 데이터베이스에 존재하지 않음",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ResponseEntity<TokenResponse> tokenRefresh(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        // Refresh token 재발급
        List<String> tokens = authService.refreshToken(tokenRefreshRequest.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(tokens.get(0), tokens.get(1)));
    }

    @GetMapping("/validate")
    @Operation(
            summary = "Access token 유효성 검사 API",
            description = "헤더에 첨부한 Access token의 유효성 여부를 검사하는 API",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "유효한 토큰",
                            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "만료된 토큰",
                            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
                    )
            }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String bearerToken) {
        // accessToken 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token format");
        }
        String accessToken = bearerToken.replace("Bearer ", "");
        boolean isValid = authService.validateAccessToken(accessToken);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Valid");
    }
}

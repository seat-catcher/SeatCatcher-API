package com.sullung2yo.seatcatcher.user.controller;


import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.response.TokenResponse;
import com.sullung2yo.seatcatcher.user.service.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/user/authenticate")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "사용자 인증 관련 API")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/apple")
    @Operation(
            summary = "애플로 로그인하기",
            description = "애플 OAuth 인증 API",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 애플 인증 성공 및 인증 토큰 발급",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버에서 토큰이 정상적으로 생성되지 않은 경우",
                            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
                    )
            }
    )
    public ResponseEntity<?> authenticateApple(@Valid @RequestBody AppleAuthRequest appleAuthRequest) {
        // RequestBody로 제대로 들어왔는지 검증
        if (appleAuthRequest.getIdentityToken() == null || appleAuthRequest.getIdentityToken().isEmpty()) {
            throw new IllegalArgumentException("Apple 인증 요청에 필요한 identityToken이 제공되지 않았습니다.");
        }
        log.debug("Authenticate with Apple: {}", appleAuthRequest);

        // 인증 로직 수행 후 토큰 생성
        List<String> tokens = authServiceImpl.authenticate(appleAuthRequest, Provider.APPLE);

        return returnAfterTokenValidation(tokens);
    }


    @PostMapping("/kakao")
    @Operation(
            summary = "카카오로 로그인하기",
            description = "카카오 OAuth 인증 API",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 카카오 인증 성공 및 인증 토큰 발급",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버에서 토큰이 정상적으로 생성되지 않은 경우",
                            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
                    )
            }
    )
    public ResponseEntity<?> authenticateKakao(@Valid @RequestBody KakaoAuthRequest kakaoAuthRequest) {
        try {
            // RequestBody로 제대로 들어왔는지 검증
            if (kakaoAuthRequest.getAccessToken() == null || kakaoAuthRequest.getAccessToken().isEmpty()) {
                throw new IllegalArgumentException("Kakao 인증 요청에 필요한 accessToken이 제공되지 않았습니다.");
            }

            // 인증 로직 수행 후 토큰 생성
            List<String> tokens = authServiceImpl.authenticate(kakaoAuthRequest, Provider.KAKAO);

            return returnAfterTokenValidation(tokens);
        } catch (Exception e) {
            throw new RuntimeException("카카오 인증 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<?> returnAfterTokenValidation(List<String> tokens) {
        if (tokens == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("인증 실패: 인증 토큰이 정상적으로 생성되지 않았습니다.");
        }

        if (tokens.size() < 2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("토큰 생성 실패: 필요한 토큰이 생성되지 않았습니다.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TokenResponse(tokens.get(0), tokens.get(1)));
    }
}

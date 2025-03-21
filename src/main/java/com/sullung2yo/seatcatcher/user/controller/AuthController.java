package com.sullung2yo.seatcatcher.user.controller;


import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.response.TokenResponse;
import com.sullung2yo.seatcatcher.user.service.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth API", description = "User Authentication APIs")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/apple")
    @Operation(summary = "Authenticate with Apple", description = "Apple OAuth2 API")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<?> authenticateApple(@RequestBody AppleAuthRequest appleAuthRequest) {
        try {
            // RequestBody로 제대로 들어왔는지 검증
            if (appleAuthRequest.getIdentityToken() == null || appleAuthRequest.getIdentityToken().isEmpty()) {
                throw new IllegalArgumentException("Apple 인증 요청에 필요한 identityToken이 제공되지 않았습니다.");
            }
            log.debug("Authenticate with Apple: {}", appleAuthRequest);

            List<String> tokens = authServiceImpl.authenticate(appleAuthRequest, Provider.APPLE);

            return returnAfterTokenValidation(tokens);
        } catch (Exception e) {
            throw new RuntimeException("애플 인증 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }


    @PostMapping("/kakao")
    @Operation(summary = "Authenticate with Kakao", description = "Kakao OAuth2 API")
    @ApiResponse(responseCode = "201", description = "Created")
    public ResponseEntity<?> authenticateKakao(@RequestBody KakaoAuthRequest kakaoAuthRequest) {
        try {
            // RequestBody로 제대로 들어왔는지 검증
            if (kakaoAuthRequest.getAccessToken() == null || kakaoAuthRequest.getAccessToken().isEmpty()) {
                throw new IllegalArgumentException("Kakao 인증 요청에 필요한 accessToken이 제공되지 않았습니다.");
            }

            List<String> tokens = authServiceImpl.authenticate(kakaoAuthRequest, Provider.KAKAO);

            return returnAfterTokenValidation(tokens);
        } catch (Exception e) {
            throw new RuntimeException("카카오 인증 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 로컬 사용자 인증 요청을 처리하는 POST 엔드포인트입니다. (미구현)
     * 로컬 인증 요청에 대해 "hello world local" 문자열 응답을 반환합니다.
     *
     * @return "hello world local" 응답 문자열
     */
    @PostMapping("/local")
    public String authenticateLocal() {
        // TODO: Implement local authentication
        return "hello world local";
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

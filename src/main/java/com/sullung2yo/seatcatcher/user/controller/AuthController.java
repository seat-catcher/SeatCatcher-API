package com.sullung2yo.seatcatcher.user.controller;


import com.sullung2yo.seatcatcher.user.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.response.TokenResponse;
import com.sullung2yo.seatcatcher.user.service.AuthServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/authenticate")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "User Authentication APIs")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/apple")
    public ResponseEntity<TokenResponse> authenticateApple(@RequestBody AppleAuthRequest appleAuthRequest) {
        try {
            List<String> tokens = authServiceImpl.authenticate(appleAuthRequest);
            if (tokens.size() < 2) {
                throw new IllegalStateException("토큰 생성 실패: 필요한 토큰이 생성되지 않았습니다.");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(tokens.get(0), tokens.get(1)));
        } catch (Exception e) {
            throw new RuntimeException("애플 인증 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 카카오 인증을 수행하여 두 개의 토큰을 반환합니다.
     * 주어진 KakaoAuthRequest 객체를 기반으로 AuthService의 인증 메서드를 호출하여,
     * Provider.KAKAO 방식의 인증을 진행합니다. 반환된 토큰 리스트에서 첫 번째와 두 번째 토큰을 추출한 후,
     * 두 토큰을 포함하는 TokenResponse 객체로 변환하여 반환합니다.
     * 토큰 리스트의 크기가 2개 미만인 경우, 즉 Access 또는 Refresh 토큰 생성 중 하나라도 실패하는 경우
     * "토큰 생성 실패: 필요한 토큰이 생성되지 않았습니다."라는 메시지와 함께 IllegalStateException이 발생하며,
     * 인증 처리 중 다른 예외가 발생할 경우 RuntimeException으로 전달됩니다.
     *
     * @param kakaoAuthRequest 카카오 인증 요청 정보를 담은 KakaoAuthRequest 객체
     * @return 두 개의 토큰을 포함하는 TokenResponse 객체
     */
    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> authenticateKakao(@RequestBody KakaoAuthRequest kakaoAuthRequest) {
        try {
            List<String> tokens = authServiceImpl.authenticate(kakaoAuthRequest);
            if (tokens.size() < 2) {
                throw new IllegalStateException("토큰 생성 실패: 필요한 토큰이 생성되지 않았습니다.");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(tokens.get(0), tokens.get(1)));
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
}

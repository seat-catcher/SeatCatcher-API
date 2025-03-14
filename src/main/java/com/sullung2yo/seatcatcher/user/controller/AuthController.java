package com.sullung2yo.seatcatcher.user.controller;


import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.dto.request.TokenReqeust;
import com.sullung2yo.seatcatcher.user.dto.response.TokenResponse;
import com.sullung2yo.seatcatcher.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/authenticate")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/apple")
    public String authenticateApple() {
        return "hello world apple";
    }

    @PostMapping("/kakao")
    public TokenResponse authenticateKakao(@RequestBody TokenRequest tokenRequest) {
        try {
            List<String> tokens = authService.authenticate(Provider.KAKAO, tokenRequest);
            if (tokens.size() < 2) {
                throw new IllegalStateException("토큰 생성 실패: 필요한 토큰이 생성되지 않았습니다.");
            }
            return new TokenResponse(tokens.get(0), tokens.get(1));
        } catch (Exception e) {
            throw new RuntimeException("카카오 인증 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @PostMapping("/local")
    public String authenticateLocal() {
        return "hello world local";
    }
}

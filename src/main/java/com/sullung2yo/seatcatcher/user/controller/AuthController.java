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

    /**
     * 애플 인증 요청을 처리하는 POST 엔드포인트입니다.
     *
     * 이 메서드는 "/apple" 경로에 대한 요청을 처리하며, "hello world apple" 문자열을 응답합니다.
     *
     * @return "hello world apple" 문자열
     */
    @PostMapping("/apple")
    public String authenticateApple() {
        return "hello world apple";
    }

    /**
     * 카카오 인증을 수행하여 두 개의 토큰을 반환합니다.
     *
     * 주어진 TokenRequest 객체를 기반으로 AuthService의 인증 메서드를 호출하여, 
     * Provider.KAKAO 방식의 인증을 진행합니다. 반환된 토큰 리스트에서 첫 번째와 두 번째 토큰을 추출한 후,
     * 두 토큰을 포함하는 TokenResponse 객체로 변환하여 반환합니다.
     *
     * 토큰 리스트의 크기가 2개 미만인 경우, "토큰 생성 실패: 필요한 토큰이 생성되지 않았습니다."라는 메시지와 함께
     * IllegalStateException이 발생하며, 인증 처리 중 다른 예외가 발생할 경우 RuntimeException으로 전달됩니다.
     *
     * @param tokenRequest 카카오 인증 요청 정보를 담은 TokenRequest 객체
     * @return 두 개의 토큰을 포함하는 TokenResponse 객체
     */
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

    /**
     * 로컬 사용자 인증 요청을 처리하는 POST 엔드포인트입니다.
     *
     * 로컬 인증 요청에 대해 "hello world local" 문자열 응답을 반환합니다.
     *
     * @return "hello world local" 응답 문자열
     */
    @PostMapping("/local")
    public String authenticateLocal() {
        return "hello world local";
    }
}

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
    public TokenResponse authenticateKakao(TokenReqeust tokenReqeust) throws Exception {
        List<String> tokens = authService.authenticate(Provider.KAKAO, tokenReqeust);
        return new TokenResponse(tokens.get(0), tokens.get(1));
    }

    @PostMapping("/local")
    public String authenticateLocal() {
        return "hello world local";
    }
}

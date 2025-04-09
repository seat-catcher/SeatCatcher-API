package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getUserInformation(@RequestHeader("Authorization") String bearerToken) throws {
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.substring(7);

        // JWT에서 사용자 정보 추출
        String

    }
}

package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.response.TagResponse;
import com.sullung2yo.seatcatcher.user.dto.response.UserInformationResponse;
import com.sullung2yo.seatcatcher.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    @GetMapping("/me")
    public ResponseEntity<UserInformationResponse> getUserInformation(@RequestHeader("Authorization") String bearerToken) {
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.substring(7);

        // JWT에서 사용자 정보 추출 및 사용자 정보 반환
        User user = userServiceImpl.getUserWithToken(token);

        return ResponseEntity.status(HttpStatus.OK).body(
                UserInformationResponse.builder()
                        .name(user.getName())
                        .profileImageNum(user.getProfileImageNum())
                        .credit(user.getCredit())
                        .tag(TagResponse.builder().build()) // TODO : TagResponse 구현해야함
                        .build()
        );
    }
}

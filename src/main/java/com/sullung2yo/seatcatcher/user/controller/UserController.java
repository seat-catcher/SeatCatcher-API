package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import com.sullung2yo.seatcatcher.user.dto.response.UserInformationResponse;
import com.sullung2yo.seatcatcher.user.service.UserService;
import com.sullung2yo.seatcatcher.user.service.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User API", description = "User APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "사용자 정보 조회 API",
            description = "AccessToken에 담긴 사용자의 정보를 조회합니다.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "사용자 정보 조회 성공",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInformationResponse.class))
                )
            }
    )
    public ResponseEntity<UserInformationResponse> getUserInformation(@RequestHeader("Authorization") String bearerToken) {
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("올바른 JWT 형식이 아닙니다.");
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.replace("Bearer ", "");
        log.debug("JWT 파싱 성공");

        // JWT에서 사용자 정보 추출 및 사용자 정보 반환
        User user = userService.getUserWithToken(token);
        List<UserTagType> tags = user.getUserTag().stream()
                        .map(userTag -> userTag.getTag().getTagName())
                        .toList();

        log.debug("사용자 정보: {}, {}, {}, {}", user.getName(), user.getCredit(), user.getProfileImageNum(), tags);
        return ResponseEntity.status(HttpStatus.OK).body(
                UserInformationResponse.builder()
                        .name(user.getName())
                        .profileImageNum(user.getProfileImageNum())
                        .credit(user.getCredit())
                        .tags(tags)
                        .build()
        );
    }
}

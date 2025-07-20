package com.sullung2yo.seatcatcher.domain.user.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import com.sullung2yo.seatcatcher.domain.user.dto.request.UserInformationUpdateRequest;
import com.sullung2yo.seatcatcher.domain.user.dto.response.UserInformationResponse;
import com.sullung2yo.seatcatcher.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return getUserInformationResponseResponseEntity(user);
    }

    @PatchMapping("/me")
    @Operation(
            summary = "사용자 정보 업데이트 API",
            description = "AccessToken에 담긴 사용자를 대상으로, 정보를 업데이트합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "사용자 정보 업데이트 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInformationResponse.class))
                    )
            }
    )
    public ResponseEntity<UserInformationResponse> updateUserInformation(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody UserInformationUpdateRequest userInformationUpdateRequest
    ) {
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("올바른 JWT 형식이 아닙니다.");
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.replace("Bearer ", "");
        log.debug("JWT 파싱 성공");

        // JWT에서 사용자 정보 추출 및 사용자 정보 업데이트
        User user = userService.updateUser(token, userInformationUpdateRequest);
        log.debug("사용자 정보 업데이트 성공");

        return getUserInformationResponseResponseEntity(user);
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "사용자 계정 삭제 API",
            description = "AccessToken에 담긴 사용자 계정을 완전히 삭제합니다. " +
                         "Apple 사용자의 경우에는 앱스토어 가이드라인에 따라 Apple 토큰도 함께 취소됩니다. " +
                         "모든 개인 데이터가 영구적으로 삭제되며 복구할 수 없습니다." +
                         "그리고 계정 삭제 전 반드시 user update API를 통해서 apple authorization code를 갱신해줘야" +
                         "애플 계정 삭제가 정상적으로 이루어집니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "사용자 계정 삭제 성공"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 - 유효하지 않은 토큰"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String bearerToken
    ) {
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("올바른 JWT 형식이 아닙니다.");
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.replace("Bearer ", "");
        log.debug("JWT 파싱 성공");

        log.info("사용자 계정 삭제 요청 시작");
        userService.deleteUser(token);
        log.info("사용자 계정 삭제 요청 완료");

        return ResponseEntity.noContent().build();
    }

    /**
     * 사용자 정보를 ResponseEntity로 변환하는 메서드
     * @param user 사용자 정보
     * @return ResponseEntity<UserInformationResponse>
     */
    private ResponseEntity<UserInformationResponse> getUserInformationResponseResponseEntity(User user) {
        List<UserTagType> tags = user.getUserTag().stream()
                .map(userTag -> userTag.getTag().getTagName())
                .toList();

        log.debug("사용자 정보: {}, {}, {}, {}", user.getName(), user.getCredit(), user.getProfileImageNum(), tags);
        return ResponseEntity.status(HttpStatus.OK).body(
                UserInformationResponse.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .profileImageNum(user.getProfileImageNum())
                        .credit(user.getCredit())
                        .tags(tags)
                        .hasOnBoarded(user.getHasOnBoarded())
                        .build()
        );
    }
}

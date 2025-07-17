package com.sullung2yo.seatcatcher.domain.user_status.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.domain.user_status.dto.request.UserStatusRequest;
import com.sullung2yo.seatcatcher.domain.user_status.dto.response.UserStatusResponse;
import com.sullung2yo.seatcatcher.domain.user_status.service.UserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/status")
@Tag(name = "UserStatus API", description = "User Status APIs")
public class UserStatusController {

    private final UserStatusService userStatusService;

    @GetMapping
    @Operation(
        summary = "유저의 Status 를 DB 에서 조회하는 API",
            description = "유저의 Status 를 DB 에서 조회합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Status 조회 성공",
                            content = @Content(schema = @Schema(implementation = UserStatusResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Status 조회 실패 (아마 token 에서 User 를 추출할 수 없는 경우 발생)"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Status 를 찾을 수 없음."
                    )
            }
    )
    public ResponseEntity<UserStatusResponse> getUserStatus(
            @RequestHeader("Authorization") String bearerToken
    )
    {
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("올바른 JWT 형식이 아닙니다.");
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.replace("Bearer ", "");
        log.debug("JWT 파싱 성공");

        return ResponseEntity.ok(userStatusService.getUserStatusWithToken(token));
    }

    @PostMapping
    @Operation(
        summary = "유저의 Status 를 DB 에 백업할 때 쓰는 API",
            description = "유저의 Status 정보를 DB 에 Entity 로써 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프론트에서 관리하던 Status 를 담을 Request 입니다.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserStatusRequest.class))
            ),
            responses = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Status 생성 성공"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Status 생성 실패 (아마 token 에서 User 를 추출할 수 없는 경우 발생)"
                )
            }
    )
    public ResponseEntity<?> createUserStatus(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody UserStatusRequest userStatusRequest
    ){
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("올바른 JWT 형식이 아닙니다.");
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.replace("Bearer ", "");
        log.debug("JWT 파싱 성공");

        userStatusService.createUserStatus(token, userStatusRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping
    @Operation(
            summary = "유저의 Status 를 변경할 때 쓰는 API",
            description = "유저의 Status 정보를 변경합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프론트에서 관리하던 Status 를 담을 Request 입니다.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserStatusRequest.class))
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Status 변경 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "변경할 Status 를 찾을 수 없음."
                    )
            }
    )
    public ResponseEntity<?> updateUserStatus(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody UserStatusRequest userStatusRequest
    ){
        // Bearer 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("올바른 JWT 형식이 아닙니다.");
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.replace("Bearer ", "");
        log.debug("JWT 파싱 성공");

        userStatusService.updateUserStatus(token, userStatusRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

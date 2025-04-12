package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.request.UserTrainSeatRequest;
import com.sullung2yo.seatcatcher.train.dto.response.UserTrainSeatResponse;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
import com.sullung2yo.seatcatcher.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/seats")
@Tag(name = "착석 정보 API", description = "착석, 자리 양도 (소유권 해제) 등을 관리하는 API")
public class UserTrainSeatController {

    private final UserTrainSeatService userTrainSeatService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "착석 정보를 조회하는 API",
            description = "현재 등록된 유저에 대한 착석 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 착석 정보를 조회했습니다.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserTrainSeatResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "해당 유저는 착석 정보가 없습니다."
                    )
            }
    )
    public ResponseEntity<UserTrainSeatResponse> getSittingInfo(@RequestHeader("Authorization") String bearerToken)
    {
        Long uid = verifyUserAndGetId(bearerToken);

        try
        {
            UserTrainSeat record = userTrainSeatService.findUserTrainSeatByUserId(uid);
            return ResponseEntity.ok(new UserTrainSeatResponse(record));
        }
        catch(EntityNotFoundException e)
        {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping
    @Operation(
            summary = "착석 정보를 생성하는 API",
            description = "좌석 id와 유저 id를 이용하여 착석 정보(매핑 정보)를 만들어줍니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "착석 정보입니다. User ID의 경우 알아낼 방법이 있지만, 그럼에도 불구하고 명시적으로 두 정보 모두 채워주세요!",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserTrainSeatRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 생성 완료"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 유저, 혹은 좌석 ID를 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<Void> createSittingInfo(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody UserTrainSeatRequest userTrainSeatRequest
    )
    {
        try
        {
            userTrainSeatService.create(userTrainSeatRequest.getUserId(), userTrainSeatRequest.getSeatId());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch(EntityNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    @Operation(
            summary = "착석 정보를 제거하는 API",
            description = "현재 등록된 유저에 대한 착석 정보를 제거, 즉 하차 처리를 수행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 제거 완료"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 유저에 대한 착석 정보를 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<Void> deleteSittingInfo(@RequestHeader("Authorization") String bearerToken)
    {
        // Bearer 토큰 검증
        Long uid = verifyUserAndGetId(bearerToken);

        try{
            userTrainSeatService.delete(uid);
            return ResponseEntity.ok().build();
        }
        catch(EntityNotFoundException e)
        {
            return ResponseEntity.notFound().build();
        }
    }

    //TODO :: 지금은 이렇게 만들지만 나중에는 AOT 등으로 자동으로 인증이 필요한 API 에 대해서 해당 로직을 수행할 수 있으면 좋을 듯.
    // 혹은 단순하게 AuthService 등에 해당 로직을 옮겨놓는 것도 좋을 듯.
    private Long verifyUserAndGetId(String bearerToken)
    {
        String token = verify(bearerToken);
        // JWT에서 사용자 정보 추출 및 사용자 정보 반환
        return userService.getUserWithToken(token).getId();
    }

    private String verify(String bearerToken)
    {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("올바른 JWT 형식이 아닙니다.");
            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
        }
        String token = bearerToken.replace("Bearer ", "");
        log.debug("JWT 파싱 성공");
        return token;
    }
}

package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.dto.request.SeatYieldRequest;
import com.sullung2yo.seatcatcher.train.dto.request.UserTrainSeatRequest;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
import com.sullung2yo.seatcatcher.train.utility.SeatInfoResponseAssembler;
import com.sullung2yo.seatcatcher.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/seats")
@Tag(name = "착석 정보 API", description = "착석, 자리 양도 (소유권 해제) 등을 관리하는 API")
public class UserTrainSeatController {

    private final UserTrainSeatService userTrainSeatService;
    private final TrainSeatGroupService trainSeatGroupService;
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "착석 정보를 조회하는 API",
            description = "착석 정보를 조회하는 API입니다. (Websocket 연결 후 trainCode로 구독했을 때, 이 API를 통해 초기 착석 정보를 가져올 수 있습니다.)",
            parameters = {
                    @Parameter(name = "trainCode", description = "열차 코드"),
                    @Parameter(name = "carCode", description = "차량 코드"),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 조회 완료",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = SeatInfoResponse.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    )
            }
    )
    public ResponseEntity<List<SeatInfoResponse>> getSeatInformation(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam String trainCode,
            @RequestParam String carCode
    ) {
        /*
         * 착석 정보 조회 API
         * Websocket 연결 후 trainCode로 구독했을 때,
         * 이 API를 통해 초기 착석 정보를 가져올 수 있습니다.
         */
        Long userId = verifyUserAndGetId(bearerToken);
        if (userId == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        // 좌석 그룹 정보 가져오기
        List<TrainSeatGroup> trainSeatGroups = trainSeatGroupService.findAllByTrainCodeAndCarCode(trainCode, carCode);
        if (trainSeatGroups.isEmpty()) {
            log.warn("해당 열차 코드 : " + trainCode + "와 차량 코드 : " + carCode + "로 생성된 좌석 그룹이 없습니다. 새로 생성합니다.");
            trainSeatGroups = trainSeatGroupService.createGroupsOf(trainCode, carCode);
        }

        // 응답 구조 생성
        List<SeatInfoResponse> response = trainSeatGroupService.createSeatInfoResponse(trainSeatGroups);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    @Operation(
            summary = "착석 정보를 생성하는 API",
            description = "좌석 id와 유저 id를 이용하여 착석 정보(매핑 정보)를 만들어줍니다.",
            requestBody = @RequestBody(
                    description = "",
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
        // Bearer 토큰 검증
        Long userId = verifyUserAndGetId(bearerToken);
        if (userId == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        userTrainSeatService.reserveSeat(userId, userTrainSeatRequest.getSeatId());
        log.debug("성공적으로 좌석 점유 지정 완료");

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @Operation(
            summary = "착석 정보를 제거하는 API",
            description = "현재 등록된 유저에 대한 착석 정보를 제거하는 API",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "성공적으로 제거 완료"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없거나, 사용자가 앉은 좌석 정보가 데이터베이스에 없는 경우"
                    )
            }
    )
    public ResponseEntity<Void> deleteSittingInfo(@RequestHeader("Authorization") String bearerToken)
    {
        // Bearer 토큰 검증
        Long userId = verifyUserAndGetId(bearerToken);
        if (userId == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        userTrainSeatService.releaseSeat(userId);
        log.debug("성공적으로 좌석 점유 해제 완료");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/yield")
    @Operation(
            summary = "유저간의 자리 교환을 수행하는 API",
            description = "좌석 id와 유저 id를 이용하여 자리 교환을 수행합니다. 반드시!! 좌석을 양보하는 쪽에서만 최초 한 번 요청되어야 합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = SeatYieldRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 교환 (새로운 좌석 점유 정보 생성) 완료"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 유저, 혹은 좌석 ID를 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<Void> yieldSeat(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody SeatYieldRequest yieldRequest
    )
    {
        // Bearer 토큰 검증
        Long givingUserId;
        Long takingUserId;

        givingUserId = verifyUserAndGetId(bearerToken);
        if (givingUserId == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }
        takingUserId = yieldRequest.getTakerId();

        userTrainSeatService.yieldSeat(yieldRequest.getSeatId(), givingUserId, takingUserId);
        log.debug("성공적으로 좌석 교환 완료");

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //TODO :: 지금은 이렇게 만들지만 나중에는 AOP 등으로 자동으로 인증이 필요한 API 에 대해서 해당 로직을 수행할 수 있으면 좋을 듯.
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

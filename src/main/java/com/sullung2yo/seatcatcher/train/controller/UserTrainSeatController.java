package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.train.dto.request.SeatYieldRequest;
import com.sullung2yo.seatcatcher.train.dto.request.UserTrainSeatRequest;
import com.sullung2yo.seatcatcher.train.service.FcmService;
import com.sullung2yo.seatcatcher.train.service.SeatEventService;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
import com.sullung2yo.seatcatcher.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.NonNull;
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
    private final SeatEventService seatEventService;
    private final FcmService fcmService;

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

        // 좌석 변경 이벤트 발생
        // TODO :: 좌석 변경 이벤트 발생 코드 추가 필요

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @Operation(
            summary = "착석 정보를 제거하는 API",
            description = "현재 등록된 유저에 대한 착석 정보를 제거하는 API",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "성공적으로 좌석 점유 해제 완료"
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

        // TODO :: 좌석 변경 이벤트 발생 코드 추가 필요

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

        // TODO :: 좌석 변경 이벤트 발생 코드 추가 필요

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 양보 요청 시 호출하는 API
     * 양보 요청을 보낸 사용자의 정보는 JWT에서 추출해서 사용하면 되고,
     * 양보 요청을 받은 사용자의 정보는 쿼리 파라미터로 전달받습니다.
     * 클라이언트 측에서 이 API를 호출 후, topic seat.seat_id로 구독을 수행해야 합니다.
     * 이 때, 좌석을 차지하고 있는 사람은, 이미 topic seat.seat_id를 구독한 상태입니다.
     * 즉, 좌석을 점유할 때 topic을 구독해야 합니다.
     * @param bearerToken JWT
     * @param seatId 양보 대상 좌석 ID
     * @return ResponseEntity
     */
    @GetMapping("/{seatId}/yield-request")
    public ResponseEntity<?> seatYieldRequest(
            @RequestHeader("Authorization") String bearerToken,
            @NonNull @PathVariable("seatId") Long seatId, // 양보 대상 좌석 ID
            @RequestParam(value = "occupantId") Long occupantId // 양보 요청을 받은 사용자 ID
    ) {
        // 양보를 요청한 사용자 정보 획득
        Long requestUserId = verifyUserAndGetId(bearerToken);

        // 양보를 요청 받은 사용자에게 토스트 메세지 띄우도록 웹소켓 메세지 전송 ()
        seatEventService.issueSeatYieldRequestEvent(seatId, requestUserId);
        log.debug("좌석 점유자에게 양보 요청 WebSocket 메세지 전송 완료");

        // FCM data push
        // 양보를 요청 받은 사용자에게 푸시 알림 전송
        fcmService.sendSeatYieldRequestNotification(occupantId, "양보 요청", "양보 요청이 도착했습니다.", seatId);
        log.debug("양보 요청 FCM 푸시 알림 전송 완료");

        return ResponseEntity.ok().build();
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

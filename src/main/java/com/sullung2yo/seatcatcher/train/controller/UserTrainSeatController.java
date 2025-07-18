package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;
// import com.sullung2yo.seatcatcher.train.dto.request.UserTrainSeatRequest;
import com.sullung2yo.seatcatcher.train.service.SeatEventService;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
import com.sullung2yo.seatcatcher.domain.credit.enums.CreditPolicy;
import com.sullung2yo.seatcatcher.domain.credit.service.CreditService;
import com.sullung2yo.seatcatcher.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/seats")
@Tag(name = "착석 정보 API", description = "착석, 자리 양도 (소유권 해제) 등을 관리하는 API")
public class UserTrainSeatController {

    private final UserTrainSeatService userTrainSeatService;
    private final UserService userService;
    private final SeatEventService seatEventService;
    private final CreditService creditService;

    @PostMapping
    @Operation(
            summary = "양보X, 그냥 좌석에 앉을 때 해당 정보를 생성하는 API",
            description = "좌석 id와 유저 id를 이용하여 착석 정보(매핑 정보)를 만들어줍니다.",
//            requestBody = @RequestBody(
//                    description = "",
//                    required = true,
//                    content = @Content(schema = @Schema(implementation = UserTrainSeatRequest.class))
//            ),
            parameters = {
                    @Parameter(name = "seatId", description = "유저가 앉을 자리의 ID입니다.", required = true),
                    @Parameter(name = "creditAmount", description = "유저 간에 오고 가는 크레딧 양입니다. (선택적, 없으면 null)", required = false)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 좌석 정보 생성 완료"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 유저, 혹은 좌석 ID를 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<Void> createSeat(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestParam(value="seatId") Long seatId, // 좌석 ID,
            @RequestParam(value = "creditAmount", required = false) Optional<Long> creditAmount // 크레딧 수량(선택적, 없으면 null)
    ) {
        log.info("좌석 점유 요청: {}, {}", seatId, creditAmount);
        // Bearer 토큰 검증
        Long userId = verifyUserAndGetId(bearerToken);
        if (userId == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        UserTrainSeat userSeat = userTrainSeatService.reserveSeat(userId, seatId);
        log.info("성공적으로 좌석 점유 지정 완료 / userId : {}", userId);

        // 좌석 변경 이벤트 생성
        TrainSeatGroup trainSeatGroup = userSeat.getTrainSeat().getTrainSeatGroup();
        seatEventService.publishSeatEvent(trainSeatGroup.getTrainCode(), trainSeatGroup.getCarCode());
        log.info("좌석 변경 이벤트 생성 완료");

        // 크레딧 추가
        creditService.creditModification(userId, CreditPolicy.CREDIT_FOR_SIT_INFO_PROVIDE.getCredit(), true, YieldRequestType.NONE);
        log.info("좌석 정보 제공에 대한 크레딧 보상 제공 완료");

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping
    @Operation(
            summary = "좌석 정보 소유자를 수정하는 API",
            description = "유저가 앉아있던 좌석의 소유자를 교체하는 API입니다. 좌석 양보 수락 시 이 API를 호출하면, 좌석 소유자가 변경되고 양보 수락 사용자에게는 크레딧이 지급됩니다.",
            parameters = {
                    @Parameter(name = "seatId", description = "유저가 앉을 자리의 ID입니다.", required = true),
                    @Parameter(name = "creditAmount", description = "유저 간에 오고 가는 크레딧 양입니다. (선택적, 없으면 null)", required = false)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 좌석 점유 해제 완료"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없거나, 사용자가 앉은 좌석 정보가 데이터베이스에 없는 경우"
                    )
            }
    )
    public ResponseEntity<Void> updateSeat(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestParam(value="seatId") Long seatId, // 좌석 ID,
            @RequestParam(value = "creditAmount", required = false) Optional<Long> creditAmount // 크레딧 수량(선택적, 없으면 null)
    ) {
        // Bearer 토큰 검증
        Long requestUserId = verifyUserAndGetId(bearerToken);
        if (requestUserId == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        userTrainSeatService.updateSeatOwner(requestUserId, seatId, creditAmount.orElse(0L));

        // 좌석 변경 이벤트 생성
        TrainSeatGroup trainSeatGroup = userTrainSeatService.findUserTrainSeatBySeatId(seatId).getTrainSeat().getTrainSeatGroup();
        seatEventService.publishSeatEvent(trainSeatGroup.getTrainCode(), trainSeatGroup.getCarCode());
        log.info("좌석 변경 이벤트 생성 완료");

        //TODO :: FCM 좌석 변경 성공 알림 보내는 로직 추가되어야 함.

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(
            summary = "양보X, 좌석 정보를 제거하는 API",
            description = "유저가 앉아있던 좌석의 정보를 제거하는 API",
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
    public ResponseEntity<Void> deleteSeat(@RequestHeader("Authorization") String bearerToken)
    {
        // Bearer 토큰 검증
        Long userId = verifyUserAndGetId(bearerToken);
        if (userId == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        TrainSeatGroup trainSeatGroup = userTrainSeatService.releaseSeat(userId);
        log.info("성공적으로 좌석 점유 해제 완료");

        // 좌석 변경 이벤트 생성
        seatEventService.publishSeatEvent(trainSeatGroup.getTrainCode(), trainSeatGroup.getCarCode());
        log.info("좌석 변경 이벤트 생성 완료");

        // 5분 내로 좌석 정보를 삭제한 경우, 크레딧 회수 (서비스 내부에서 조건 검증 및 처리)
        creditService.creditModification(userId, CreditPolicy.CREDIT_FOR_SIT_INFO_PROVIDE.getCredit(), false, YieldRequestType.NONE);

        return ResponseEntity.noContent().build();
    }

    /**
     * 양보 요청 시 호출하는 API
     * 양보 요청을 보낸 사용자의 정보는 JWT에서 추출해서 사용하면 되고,
     * 양보 요청을 받은 사용자의 정보는 쿼리 파라미터로 전달받습니다.
     * 클라이언트 측에서 이 API를 호출 후, topic seat.{seat_id}.requester.{user_id}로 구독을 수행해야 합니다.
     * 이 때, 좌석을 차지하고 있는 사람은, topic seat.{seat_id}와 seat.{seat_id}.owner를 구독한 상태입니다.
     * 즉, 좌석을 점유할 때 topic을 구독해야 합니다.
     * @param bearerToken JWT
     * @param seatId 양보 대상 좌석 ID
     * @return ResponseEntity
     */
    @PostMapping("/{seatId}/yield")
    @Operation(
            summary = "양보 요청, 양보 수락, 양보 거절, 양보 요청 취소를 처리하는 API",
            description = "쿼리 파라미터를 통해서 수행하려는 동작을 명시해주세요.",
            parameters = {
                    @Parameter(name = "type", description = "양보 요청 타입 (양보 요청: request, 양보 수락: accept, 양보 거절: reject, 요청 취소 : cancel)", required = true),
                    @Parameter(name = "oppositeUserId", description = "상대방 사용자 ID (양보 수락/거절 시 필수적으로 전달해야하고, 그 이외에는 전달 X)", required = false),
                    @Parameter(name = "creditAmount", description = "상대방에게 제안할 크레딧 양 (양보 요청/거절/취소 시 필수적으로 전달해야 하고, 그 이외에는 전달 X)", required = false)
            },
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
    public ResponseEntity<?> seatYieldRequest(
            @RequestHeader("Authorization") String bearerToken,
            @NonNull @PathVariable("seatId") Long seatId, // 양보 대상 좌석 ID
            @RequestParam(value = "type") YieldRequestType requestType, // 양보 요청 타입 (양보 요청: request, 양보 수락: accept, 양보 거절: reject, 요청 취소 : cancel)
            @RequestParam(value = "oppositeUserId", required = false) Optional<Long> oppositeUserId, // 상대방 사용자 ID
            @RequestParam(value = "creditAmount", required = false) Optional<Long> creditAmount // 상대방에게 제안할 크레딧 양
    ) {

        // API 호출한 사람 ID 가져오기
        Long requestUserId = verifyUserAndGetId(bearerToken);

        // 양보 로직 처리
        seatEventService.publishSeatYieldEvent(
                seatId,
                requestType,
                requestUserId,
                oppositeUserId,
                creditAmount
        );
        log.debug("좌석 점유자에게 양보 요청 완료");

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

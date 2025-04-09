package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.train.dto.request.UserTrainSeatRequest;
import com.sullung2yo.seatcatcher.train.dto.response.UserTrainSeatResponse;
import com.sullung2yo.seatcatcher.train.service.TrainSeatService;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user/seats")
public class UserTrainSeatController {

    private final UserTrainSeatService userTrainSeatService;
    private final TrainSeatService trainSeatService;

    @GetMapping
    @Operation(
            summary = "착석 정보를 조회하는 API",
            description = "현재 등록된 유저에 대한 착석 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 착석 정보를 조회했습니다.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserTrainSeatResponse.class))
                    )
            }
    )
    public ResponseEntity<UserTrainSeatResponse> getSittingInfo()
    {
        // TODO:: 일단 유저 ID를 토큰에서 추출해내야 함.
        Long uid = 1L; // 일단 임시로 이걸로 하자.
        return ResponseEntity.ok(new UserTrainSeatResponse(userTrainSeatService.findUserTrainSeatByUserId(uid)));
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
                    )
            }
    )
    public ResponseEntity<Void> createSittingInfo(
            @RequestBody UserTrainSeatRequest userTrainSeatRequest
    )
    {
        userTrainSeatService.create(userTrainSeatRequest.getUserId(), userTrainSeatRequest.getSeatId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(
            summary = "착석 정보를 제거하는 API",
            description = "현재 등록된 유저에 대한 착석 정보를 제거, 즉 하차 처리를 수행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 제거 완료"
                    )
            }
    )
    public ResponseEntity<Void> deleteSittingInfo()
    {
        //TODO:: 일단 유저 ID를 토큰에서 추출해내야 함.
        Long uid = 1L; // 일단 임시로 이걸로 하자.
        userTrainSeatService.delete(uid);
        return ResponseEntity.ok().build();
    }
}

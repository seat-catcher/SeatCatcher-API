package com.sullung2yo.seatcatcher.subway_station.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.common.exception.dto.ErrorResponse;
import com.sullung2yo.seatcatcher.common.service.TaskScheduleService;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.subway_station.dto.request.StartJourneyRequest;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.dto.response.StartJourneyResponse;
import com.sullung2yo.seatcatcher.subway_station.service.PathHistoryRealtimeUpdateService;
import com.sullung2yo.seatcatcher.subway_station.service.PathHistoryService;
import com.sullung2yo.seatcatcher.train.domain.TrainArrivalState;
import com.sullung2yo.seatcatcher.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/path-histories")
@RequiredArgsConstructor
@Tag(name = "과거 경로 API", description = "과거 경로 API")
public class PathHistoryController {

    private final PathHistoryService pathHistoryService;
    private final UserService userService;
    private final TaskScheduleService scheduleService;
    private final PathHistoryRealtimeUpdateService pathHistoryRealtimeUpdateService;

    @PostMapping("/")
    @Operation(
            summary = "path history 생성 API",
            description = "특정 path history를 생성합니다.)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "PathHistory 생성 예시",
                                    summary = "기본 요청 예시",
                                    value = "{ \"startStationId\": 10, \"endStationId\": 20 }"
                            )
                    }
            )
            )
    )
    @ApiResponse(responseCode = "200", description = "path history 생성 성공")
    public ResponseEntity<?> postPathHistory(@RequestHeader("Authorization") String bearerToken, @Valid @RequestBody PathHistoryRequest request) {
        String token = bearerToken.replace("Bearer ", "");
        pathHistoryService.addPathHistory(token, request);
        return ResponseEntity.ok("pathHistory가 생성되었습니다.");
    }
    @GetMapping("/")
    @Operation(
            summary = "All path history 가져오기 API",
            description = "무한 스크롤 기반으로 모든 path history를 가져옵니다.)"

    )
    @ApiResponse(responseCode = "200", description = "All path history 가져오기 성공")
    public ResponseEntity<PathHistoryResponse.PathHistoryList> getAllPathHistory(@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) Long cursor) {
        PathHistoryResponse.PathHistoryList response = pathHistoryService.getAllPathHistory(size, cursor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{path_id}")
    @Operation(
            summary = "특정 path history 가져오기 API",
            description = "특정 path history를 가져옵니다.)"
    )
    @ApiResponse(responseCode = "200", description = "특정 path history 가져오기 성공")
    @ApiResponse(responseCode = "403", description = "user가 pathHistory에 접근할 권한이 없음")
    @ApiResponse(responseCode = "404", description = "user/pathHistory를 찾을 수 없음")
    public ResponseEntity<PathHistoryResponse.PathHistoryInfoResponse> getPathHistory(@PathVariable("path_id") Long pathId) {
        PathHistoryResponse.PathHistoryInfoResponse response = pathHistoryService.getPathHistory(pathId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{path_id}")
    @Operation(
            summary = "path history 삭제 API",
            description = "특정 path history를 삭제합니다. pathId로 요청할 수 있습니다.)"

    )
    @ApiResponse(responseCode = "200", description = "path history 삭제 성공")
    @ApiResponse(responseCode = "403", description = "user가 pathHistory에 접근할 권한이 없음")
    @ApiResponse(responseCode = "404", description = "path history를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> deletePathHistory(@PathVariable("path_id") Long pathtId) {
        pathHistoryService.deletePathHistory(pathtId);
        return ResponseEntity.ok("해당 pathHistory를 삭제했습니다.");
    }

    @PatchMapping("/{path_id}")
    @Operation(
            summary = "path history 수정 API / admin 미완성",
            description = "특정 path history를 수정합니다. pathId로 요청할 수 있습니다.)"

    )
    @ApiResponse(responseCode = "200", description = "path history 수정 성공")
    @ApiResponse(responseCode = "404", description = "path history를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> patchPathHistory(@PathVariable("path_id") Long pathtId) {

//        return ResponseEntity.ok(response);
        return null;
    }





    @PostMapping("/start-journey")
    @Operation(
            summary = "도착까지 남은 시간 주기적 갱신 API",
            description = "해당 API 호출 시점부터 차량에 탑승한 것으로 판단, 도착까지 남은 시간을 주기적으로 계산하여 갱신합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "성공적으로 PathHistory 생성 & 스케줄링 완료.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StartJourneyResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "잘못된 요청.")
    public ResponseEntity<StartJourneyResponse> startJourney(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody StartJourneyRequest request
    )
    {
        // 토큰에서 유저 ID 를 추출
        // Bearer 토큰 검증
        Long uid = verifyUserAndGetId(bearerToken);
        if (uid == null) {
            throw new UserException("토큰에 담긴 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        //해당 유저의 제일 최신의 PathHistory 를 가져오기
        //PathHistory latestPathHistory = pathHistoryService.getUsersLatestPathHistory(uid);

        // 직접 입력받은 정보를 토대로 PathHistory 생성
        String token = bearerToken.replace("Bearer ", "");
        PathHistory latestPathHistory = pathHistoryService.addPathHistory(token, new PathHistoryRequest(request.getStartStationId(), request.getEndStationId()));

        // 해당 PathHistory 의 expectedArrivalTime 을 이용해서 남은 시간이 정확히 몇 초인지 계산.
        long remainingSeconds = pathHistoryService.getRemainingSeconds(latestPathHistory.getExpectedArrivalTime());

        if(remainingSeconds < 0 || latestPathHistory.getExpectedArrivalTime() == null)
        { // 과거 값이 들어온 경우거나 값이 올바르지 않게 들어왔을 경우
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 그리고 이걸 이용해서 언제 스케줄링되어야 하는지를 계산.
        long nextScheduleTime = pathHistoryRealtimeUpdateService.getNextScheduleTime(remainingSeconds);
        LocalDateTime nextScheduleDateTime;

        if(nextScheduleTime < 360)
        {
            nextScheduleDateTime = LocalDateTime.now().plusSeconds(nextScheduleTime);
            // 이 정도면 곧바로 실시간으로 업데이트가 가능함. 스케줄링 맡기지 말고 즉시 한 번 업데이트하자!
            pathHistoryRealtimeUpdateService.updateArrivalTimeAndSchedule(latestPathHistory, request.getTrainCode(), TrainArrivalState.STATE_NOT_FOUND);
        }
        else
        {
            // expected arrival time 이 되기 전 N초 전에 해당 스케줄을 실행.
            nextScheduleDateTime = scheduleService.runThisAtBeforeSeconds(latestPathHistory.getExpectedArrivalTime(), nextScheduleTime, ()->
            {
                pathHistoryRealtimeUpdateService.updateArrivalTimeAndSchedule(latestPathHistory, request.getTrainCode(), TrainArrivalState.STATE_NOT_FOUND);
            });
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new StartJourneyResponse(
                latestPathHistory.getId(),
                latestPathHistory.getExpectedArrivalTime(),
                nextScheduleDateTime,
                false
                ));
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

package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SubwayException;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.service.SubwayStationService;
import com.sullung2yo.seatcatcher.subway_station.utility.StationNameMapper;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/trains")
public class TrainController {

    private final StationNameMapper stationNameMapper;
    private final SubwayStationService subwayStationService;
    private final TrainSeatGroupService trainSeatGroupService;

    @GetMapping
    @Operation(
            summary = "차량 좌석 정보 조회 API",
            description = "전달된 열차 번호와 차량 번호를 기준으로 좌석 정보를 반환합니다",
            parameters = {
                    @Parameter(name = "trainCode", description = "열차 코드", required = true),
                    @Parameter(name = "carCode", description = "차량 코드", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 좌석 정보 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = SeatInfoResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류"
                    )
            }
    )
    public ResponseEntity<List<SeatInfoResponse>> handleSeatRequest(
            @NonNull @RequestParam String trainCode,
            @NonNull @RequestParam String carCode
    ) {
        // 좌석 그룹 정보 가져오기
        List<TrainSeatGroup> trainSeatGroups = trainSeatGroupService.findAllByTrainCodeAndCarCode(trainCode, carCode);
        if (trainSeatGroups.isEmpty()) {
            log.warn("해당 열차 코드 : " + trainCode + "와 차량 코드 : " + carCode + "로 생성된 좌석 그룹이 없습니다. 새로 생성합니다.");
            trainSeatGroups = trainSeatGroupService.createGroupsOf(trainCode, carCode);
        }

        // 응답 구조 생성
        List<SeatInfoResponse> responses = trainSeatGroupService.createSeatInfoResponse(trainCode, carCode, trainSeatGroups);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/incomings")
    @Operation(
            summary = "실시간 지하철 도착 정보 반환 API",
            description = "쿼리 파라미터에 전달된 노선 번호와 출발역을 기준으로 실시간 지하철 도착 정보를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 접근 열차 정보 반환",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = IncomingTrainsResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "접근하는 열차가 없는 경우"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류"
                    )
            }
    )
    public ResponseEntity<List<IncomingTrainsResponse>> getIncomingTrains(
            @NonNull @RequestParam String lineNumber, // 노선번호 (1, 2, 3, 4, 5, 6, 7, 8, 9)
            @NonNull @RequestParam String dep, // 출발역
            @NonNull @RequestParam String dest, // 도착역
            @RequestHeader("Authorization") String bearerToken
    ) {
        try {
            // Bearer 토큰 검증
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                log.error("올바른 JWT 형식이 아닙니다.");
                throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
            }
            String token = bearerToken.replace("Bearer ", "");
            log.debug("JWT 파싱 성공");

            // 동일 노선에 존재하는 역 정보 조회
            SubwayStation departure = subwayStationService.findByStationNameAndLine(dep, lineNumber);
            SubwayStation destination = subwayStationService.findByStationNameAndLine(dest, lineNumber);
            if (departure.getLine().equals(destination.getLine())) {
                // TODO : 이부분 따로 서비스에서 처리하거나 private 메서드로 분리해야 깔끔할 것 같음
                // 현재는 출발역과 도착역 노선이 동일한 경우에만 처리
                // TODO : 출발역과 도착역 노선이 다른 경우 처리 로직 추가 필요 (다익스트라 알고리즘 등...)

                // 1. 실시간 도착 정보 API 호출
                Optional<String> response = subwayStationService.fetchIncomingTrains(lineNumber, stationNameMapper.mapToApiName(departure.getStationName()));
                log.debug("실시간 도착 정보에서 정보를 반환했습니다!");

                // 2. API 응답 파싱
                if (response.isPresent()) {
                    List<IncomingTrainsResponse> incomingTrains = subwayStationService.parseIncomingResponse(
                            lineNumber, // 노선번호
                            departure, // 출발역 객체
                            destination, // 도착역 객체
                            response.get() // API 응답 Optional 해제
                    ).stream() // 3. 정렬 로직
                            .sorted( // 정렬 기준 : 도착 예정 시간 (bravlDt)
                                    Comparator.comparing(incomingTrain -> Integer.parseInt(incomingTrain.getArrivalTime()))
                            )
                            .toList();
                    log.debug("반환받은 열차 개수 : {}", incomingTrains.size());
                    // 4. 정렬된 열차 정보 반환
                    return ResponseEntity.ok().body(incomingTrains);
                } else {
                    log.info("{}으로 접근하는 열차가 없습니다.", departure.getStationName());
                    return ResponseEntity.noContent().build();
                }
            } else {
                log.error("출발역과 도착역의 노선이 다릅니다.");
                throw new SubwayException("출발역과 도착역의 노선이 다릅니다.", ErrorCode.SUBWAY_LINE_MISMATCH);
            }
        } catch (SubwayException e) {
            log.error("SubwayException 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("예외 발생: {}", e.getMessage());
            throw new SubwayException("예외 발생", ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

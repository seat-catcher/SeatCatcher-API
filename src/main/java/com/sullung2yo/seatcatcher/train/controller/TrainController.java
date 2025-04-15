package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.SubwayException;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.response.SubwayStationResponse;
import com.sullung2yo.seatcatcher.subway_station.service.SubwayStationService;
import com.sullung2yo.seatcatcher.subway_station.utility.StationNameMapper;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/trains")
public class TrainController {

    private final StationNameMapper stationNameMapper;
    private final SubwayStationService subwayStationService;

    @GetMapping("/incomings")
    @Operation(
            summary = "실시간 지하철 도착 정보 반환 API",
            description = "쿼리 파라미터에 전달된 노선 번호와 출발역을 기준으로 실시간 지하철 도착 정보를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 접근 열차 정보 반환",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubwayStationResponse.class))
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
            SubwayStation departure = subwayStationService.findByStationNameAndLine(
                    dep, lineNumber
            );
            SubwayStation destination = subwayStationService.findByStationNameAndLine(
                    dest, lineNumber
            );
            if (departure.getLine().equals(destination.getLine())) {
                // 실시간 도착 정보 API 호출
                Optional<String> response = subwayStationService.fetchIncomingTrains(lineNumber, stationNameMapper.mapToApiName(departure.getStationName()));

                // API 응답 파싱
                if (response.isPresent()) {
                    List<IncomingTrainsResponse> incomingTrains = subwayStationService.parseIncomingResponse(
                            lineNumber, // 노선번호
                            departure, // 출발역 객체
                            destination, // 도착역 객체
                            response.get() // API 응답 Optional 해제
                    );
                    return ResponseEntity.ok().body(incomingTrains);
                } else {
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

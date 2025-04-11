package com.sullung2yo.seatcatcher.subway_station.controller;

import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.response.SubwayStationResponse;
import com.sullung2yo.seatcatcher.subway_station.service.SubwayStationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/stations")
@Tag(name = "지하철 역 API", description = "지하철 역에 대한 API입니다.")
public class SubwayStationController {

    private final SubwayStationServiceImpl subwayStationService;

    @GetMapping
    @Operation(
            summary = "역 조회 API",
            description = "쿼리 파라미터를 이용하여 조건에 맞는 모든 역을 검색합니다.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "성공적으로 역 정보들 반환 완료",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubwayStationResponse.class))
                )
            }
    )
    public ResponseEntity<List<SubwayStationResponse>> getStations(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String line,
        @RequestParam(required = false) String order
    )
    {
        List<SubwayStationResponse> responses = new ArrayList<>();

        List<SubwayStation> records = subwayStationService.findWith(keyword, line, order);
        for(SubwayStation record : records)
        {
            responses.add(new SubwayStationResponse(record));
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{station_id}")
    @Operation(
            summary = "단일 역 조회 API",
            description = "지하철 역의 id를 이용하여 특정 역 하나를 검색합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 역 정보 반환 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubwayStationResponse.class))
                    )
            }
    )
    public ResponseEntity<SubwayStationResponse> getStationById(
            @PathVariable Long stationId
    )
    {
        return ResponseEntity.ok(
                new SubwayStationResponse(subwayStationService.findById(stationId))
        );
    }
}

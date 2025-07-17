package com.sullung2yo.seatcatcher.domain.subway_station.controller;

import com.sullung2yo.seatcatcher.domain.subway_station.enums.Line;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.subway_station.dto.response.SubwayStationResponse;
import com.sullung2yo.seatcatcher.domain.subway_station.service.SubwayStationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
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
                ),
                @ApiResponse(
                        responseCode = "204",
                        description = "조건을 만족하는 역이 없음"
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청"
                )
            }
    )
    public ResponseEntity<List<SubwayStationResponse>> getStations(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String line, // 유저측에서는 1, 2 이런 식으로 입력함.
        @RequestParam(defaultValue = "up") String order
    )
    {
        try
        {

            Line lineToSearch = null;
            if(line != null) lineToSearch = Line.findByName(line);

            List<SubwayStationResponse> responses = new ArrayList<>();

            List<SubwayStation> records = subwayStationService.findWith(keyword, lineToSearch, order);

            if(records == null || records.isEmpty()) {
                log.info("조건을 만족하는 역이 없음");
                return ResponseEntity.noContent().build();
            }

            for(SubwayStation record : records)
            {
                responses.add(new SubwayStationResponse(record));
            }

            return ResponseEntity.ok(responses);
        }
        catch (Exception e)
        {
            log.error("잘못된 요청");
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{stationId}")
    @Operation(
            summary = "단일 역 조회 API",
            description = "지하철 역의 id를 이용하여 특정 역 하나를 검색합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 역 정보 반환 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SubwayStationResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 ID를 가진 역을 찾을 수 없음"
                    )
            }
    )
    public ResponseEntity<SubwayStationResponse> getStationById(
            @PathVariable Long stationId
    )
    {
        try
        {
            log.debug("성공적으로 역 정보 반환 완료");
            return ResponseEntity.ok(
                    new SubwayStationResponse(subwayStationService.findById(stationId))
            );
        }
        catch(EntityNotFoundException e)
        {
            log.error("해당 ID({})를 가진 역을 찾을 수 없음", stationId);
            return ResponseEntity.notFound().build();
        }
    }
}

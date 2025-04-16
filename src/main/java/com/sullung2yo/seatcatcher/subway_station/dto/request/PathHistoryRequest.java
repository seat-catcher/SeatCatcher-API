package com.sullung2yo.seatcatcher.subway_station.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PathHistoryRequest {
    @Schema(description = "사용자 id")
    Long userId;

    @Schema(description = "시작역 id")
    Long startStationId;

    @Schema(description = "도착역 id")
    Long endStationId;
}

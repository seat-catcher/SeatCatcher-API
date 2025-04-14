package com.sullung2yo.seatcatcher.subway_station.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PathHistoryResponse {
    @Schema(description = "PathHistoryId 입니다.")
    Long id;

    @Schema(description = "시작역 id 입니다.")
    Long startStationId;

    @Schema(description = "시작역 명 입니다.")
    String startStationName;

    @Schema(description = "도착역 id 입니다.")
    Long endStationId;

    @Schema(description = "도착역 명 입니다.")
    String endStationName;

    //초 제외
    @Schema(description = "예상 도착 시간을 나타냅니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalDateTime expectedArrivalTime;

    @Schema(description = "경로 생성일을 나타냅니다.")
    String createdDate;

}

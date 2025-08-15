package com.sullung2yo.seatcatcher.domain.path_history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "경로 기록 요청 DTO")
public class PathHistoryRequest {

    @Schema(description = "시작역 id")
    private Long startStationId;

    @Schema(description = "도착역 id")
    private Long endStationId;
}

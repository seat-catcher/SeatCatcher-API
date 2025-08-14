package com.sullung2yo.seatcatcher.domain.path_history.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sullung2yo.seatcatcher.domain.subway_station.enums.Line;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PathHistoryResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @ToString
    public static class PathHistoryList {
        private List<PathHistoryInfoResponse> pathHistoryInfoList;
        private Long nextCursor; // 다음 커서 값
        private boolean isLast;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @ToString
    public static class PathHistoryInfoResponse {

        @Schema(description = "PathHistoryId 입니다.")
        private Long id;

        @Schema(description = "시작역 id 입니다.")
        private Long startStationId;

        @Schema(description = "시작역 명 입니다.")
        private String startStationName;

        @Schema(description = "시작역 호선 정보입니다.")
        private Line startline;

        @Schema(description = "도착역 id 입니다.")
        private Long endStationId;

        @Schema(description = "도착역 명 입니다.")
        private String endStationName;

        @Schema(description = "도착역 호선 정보입니다.")
        private Line endline;

        // 초 제외
        @Schema(description = "예상 도착 시간을 나타냅니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        private LocalDateTime expectedArrivalTime;

        @Schema(description = "경로 생성일을 나타냅니다.")
        private String createdDate;
    }
}
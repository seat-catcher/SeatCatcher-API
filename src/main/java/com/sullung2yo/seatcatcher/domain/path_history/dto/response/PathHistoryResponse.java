package com.sullung2yo.seatcatcher.domain.path_history.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sullung2yo.seatcatcher.domain.subway_station.enums.Line;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PathHistoryResponse {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class PathHistoryList {
        private List<PathHistoryInfoResponse> pathHistoryInfoList;
        private Long nextCursor; // 다음 커서 값
        boolean isLast;
    }
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class PathHistoryInfoResponse{

        @Schema(description = "PathHistoryId 입니다.")
        Long id;

        @Schema(description = "시작역 id 입니다.")
        Long startStationId;

        @Schema(description = "시작역 명 입니다.")
        String startStationName;

        @Schema(description = "시작역 호선 정보입니다.")
        Line startline;

        @Schema(description = "도착역 id 입니다.")
        Long endStationId;

        @Schema(description = "도착역 명 입니다.")
        String endStationName;

        @Schema(description = "도착역 호선 정보입니다.")
        Line endline;

        //초 제외
        @Schema(description = "예상 도착 시간을 나타냅니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalDateTime expectedArrivalTime;

        @Schema(description = "경로 생성일을 나타냅니다.")
        String createdDate;
    }

}
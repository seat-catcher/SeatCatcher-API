package com.sullung2yo.seatcatcher.domain.path_history.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "여정 시작 응답 DTO")
public class StartJourneyResponse {

    @Schema(description = "생성/추적중인 PathHistory 의 id")
    private Long pathHistoryId;

    @Schema(description = "PathHistory 의 Expected Arrival Time", example = "2025-06-01 14:30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expectedArrivalTime;

    @Schema(description = "디버그용 정보. 다음 스케줄이 발생할 시간.", example = "2025-06-01 14:28")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime nextScheduleTime;

    @Schema(description = "도착 여부. 해당 값이 false인 경우 expectedArrivalTime 갱신이 목표.", example = "false")
    private boolean isArrived;
}

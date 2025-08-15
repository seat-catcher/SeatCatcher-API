package com.sullung2yo.seatcatcher.domain.path_history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "여정 시작 요청 DTO")
public class StartJourneyRequest {

    @Schema(description = "출발역의 id", example = "212")
    @NotNull(message = "출발역의 ID는 필수로 입력해야 합니다!")
    private Long startStationId;

    @Schema(description = "도착역의 id", example = "213")
    @NotNull(message = "도착역의 ID는 필수로 입력해야 합니다!")
    private Long endStationId;

    @Schema(description = "유저가 현재 탑승한 열차의 Code", example = "7102")
    @NotBlank(message = "열차 코드는 필수로 입력해야 합니다!")
    private String trainCode;
}

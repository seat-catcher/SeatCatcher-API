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
public class StartJourneyRequest {
    @Schema(description = "유저가 현재 탑승한 열차의 Code")
    String trainCode;
}

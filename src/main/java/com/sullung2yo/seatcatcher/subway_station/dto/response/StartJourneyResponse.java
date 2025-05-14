package com.sullung2yo.seatcatcher.subway_station.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StartJourneyResponse {

    @Schema(description = "앞으로 추적해야 하는, 생성된 PathHistory 의 id")
    private Long pathHistoryId;

}

package com.sullung2yo.seatcatcher.domain.subway_station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubwayDataRoot {

    // Description - json 파일 참고
    @JsonProperty("DESCRIPTION")
    private Map<String, String> description;

    // Data - 실제 데이터 들어있는 Object
    @JsonProperty("DATA")
    private List<SubwayStationData> data;
}

package com.sullung2yo.seatcatcher.domain.subway_station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubwayStationData {
    /**
     * 서울 공공데이터에서 JSON 파일을 내려받아서 파싱
     * 외부 API 호출 대신 파일 파싱 용도로 사용
     */

    @JsonProperty("acml_dist")
    @Builder.Default
    private float accumulatedDistance = 0.0f;

    @JsonProperty("sbwy_rout_ln")
    @Builder.Default
    private String subwayLine = "";

    @JsonProperty("dist_km")
    @Builder.Default
    private float distanceKm = 0.0f;

    @JsonProperty("hm")
    @Builder.Default
    private String hourMinutes = "0:00";

    @JsonProperty("sbwy_stns_nm")
    @Builder.Default
    private String subwayStationName = "";

    @Override
    public String toString() {
        return String.format(
                "노선번호: %s, 역명: %s, 축적거리(km): %s, 거리(km): %s, 소요시간: %s",
                nullSafe(subwayLine),
                nullSafe(subwayStationName),
                floatSafe(accumulatedDistance),
                floatSafe(distanceKm),
                nullSafe(hourMinutes)
        );
    }

    private String nullSafe(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }

    private String floatSafe(float f) {
        return f == 0.0f ? "-" : Float.toString(f);
    }
}

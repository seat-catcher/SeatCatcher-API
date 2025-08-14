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
    private float accumulatedDistance; // 기준역부터 축적거리 (km)

    @JsonProperty("sbwy_rout_ln")
    private String subwayLine; // 지하철 노선 번호 (1, 2, ...)

    @JsonProperty("dist_km")
    private float distanceKm; // 현재 위치에서 다음 역까지 거리 (km)

    @JsonProperty("hm")
    private String hourMinutes; // 현재 위치에서 다음 역까지 소요 시간 (H:MM)

    @JsonProperty("sbwy_stns_nm")
    private String subwayStationName; // 지하철 역 이름 (서울시청, 강남역, ...)

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

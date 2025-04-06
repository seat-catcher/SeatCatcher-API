package com.sullung2yo.seatcatcher.subway_station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubwayStationData {
    /**
     * 서울 공공데이터에서 JSON 파일을 내려받아서 파싱
     * 굳이 외부 API 호출하는것보다는 파일 있는거 쓰는게 좋을 듯 해서...
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
        return "노선번호 : " + subwayLine +
                ", 역명 : " + subwayStationName +
                ", 축적거리 : " + accumulatedDistance +
                ", 거리 : " + distanceKm +
                ", 소요시간 : " + hourMinutes;
    }
}

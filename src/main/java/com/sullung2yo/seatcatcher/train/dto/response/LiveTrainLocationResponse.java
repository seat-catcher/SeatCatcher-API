package com.sullung2yo.seatcatcher.train.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LiveTrainLocationResponse {

    @JsonProperty("subwayNm")
    private String lineNumber; // 지하철 노선 번호

    @JsonProperty("statnNm")
    private String stationName; // 현재 위치 또는 다가가고 있는 지하철 역 이름

    @JsonProperty("trainNo")
    private String trainNumber; // 열차 번호

    @JsonProperty("updnLine")
    private String updownType; // 상행/하행 구분 (0: 상행, 1: 하행)

    @JsonProperty("trainSttus")
    private String trainStatus; // 열차 상태 (0: stationName에 진입, 1: stationName에 도착, 2: stationName을 출발, 3: 전역 출발)

    @JsonProperty("directAt")
    private String isDirectTrain; // 급행열차 여부 (0: 일반, 1: 급행, 7: 특급)

}

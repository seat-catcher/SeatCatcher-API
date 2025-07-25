package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(
        description = "열차 도착 정보 응답 DTO",
        title = "열차 도착 정보 응답 DTO"
)
@Tag(name="TrainSeatGroup API", description = "열차 관련 API")
@ToString
@Builder
public class IncomingTrainsResponse {

    @JsonProperty("btrainNo")
    @Schema(description = "지하철 ID", example = "1001")
    private String subwayId; // btrainNo

    @JsonProperty("ordkey")
    @Schema(description = "도착 예정 열차 순번", example = "11002온수0 -> 상하행코드1자리, 순번1자리, 현재역3자리, 목적지정류장, 급행코드1자리\n 12004석남0 -> 하행/두번째열차/004번역/석남행/급행아님")
    private String arrivalTrainOrder; // ordkey

    @JsonProperty("barvlDt")
    @Schema(description = "도착 예정 시간", example = "180(초)")
    private String arrivalTime; // barvlDt

    @JsonProperty("arvlMsg2")
    @Schema(description = "첫 번째 도착 메시지", example = "도착, 출발, 진입 등")
    private String arrivalMessage; // arvlMsg2

    @JsonProperty("arvlCd")
    @Schema(description = "도착 코드", example = "0:진입, 1:도착, 2:출발, 3:전역출발, 4:전역진입, 5:전역도착, 99:운행중")
    private Integer arrivalCode; // arvlCd

    @JsonProperty("bstatnNm")
    @Schema(description = "종착역 이름", example = "서울역")
    private String destinationStationName; // bstatnNm
}

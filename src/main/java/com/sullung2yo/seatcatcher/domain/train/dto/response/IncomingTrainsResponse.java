package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Schema(description = "열차 도착 정보 응답 DTO", title = "열차 도착 정보 응답 DTO")
@Tag(name="TrainSeatGroup API", description = "열차 관련 API")
public class IncomingTrainsResponse {

    @JsonProperty("btrainNo")
    @Schema(description = "지하철 ID", example = "1001")
    private String subwayId;

    @JsonProperty("ordkey")
    @Schema(description = "도착 예정 열차 순번", example = "11002온수0 ...")
    private String arrivalTrainOrder;

    @JsonProperty("barvlDt")
    @Schema(description = "도착 예정 시간", example = "180(초)")
    private String arrivalTime;

    @JsonProperty("arvlMsg2")
    @Schema(description = "첫 번째 도착 메시지", example = "도착, 출발, 진입 등")
    private String arrivalMessage;

    @JsonProperty("arvlCd")
    @Schema(description = "도착 코드", example = "0:진입, 1:도착 ...")
    private Integer arrivalCode;

    @JsonProperty("bstatnNm")
    @Schema(description = "종착역 이름", example = "서울역")
    private String destinationStationName;
}
package com.sullung2yo.seatcatcher.train.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(
        description = "열차 도착 정보 응답 DTO",
        title = "열차 도착 정보 응답 DTO"
)
@Tag(name="Train API", description = "열차 관련 API")
@ToString
public class IncomingTrainsResponse {

    @JsonProperty("ordkey")
    @Schema(description = "도착 예정 열차 순번", example = "1")
    private String arrivalTrainOrder; // ordkey

    @JsonProperty("barvlDt")
    @Schema(description = "도착 예정 시간", example = "2023-10-01T12:00:00")
    private String arrivalTime; // barvlDt

    @JsonProperty("bstatnNm")
    @Schema(description = "종착역 이름", example = "서울역")
    private String destinationStationName; // bstatnNm
}

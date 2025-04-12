package com.sullung2yo.seatcatcher.subway_station.dto.response;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "요청에 응답하기 위한 역에 대한 DTO입니다.")
public class SubwayStationResponse {

    @Schema(description = "역에 대한 ID입니다. Primary Key 로 사용됩니다.")
    private Long id;

    @Schema(description = "역 이름입니다.")
    private String name;

    @Schema(description = "호선 이름입니다.")
    private String line;

    @Schema(description = "전 역에서 해당 역까지 오는 데에 걸리는 시간입니다.")
    private String timeMinSec;

    @Schema(description = "처음 역에서부터 해당 역까지 오는 데에 소요되는 누계 시간입니다. 초 단위입니다.")
    private Long acmlTime;

    @Schema(description = "전 역에서부터의 거리를 km 단위로 나타낸 것입니다.")
    private Float distKm;

    @Schema(description = "처음 역에서부터 해당 역까지 오는 데에 필요한 누계 거리입니다. km 단위입니다.")
    private Float acmlDist;

    public SubwayStationResponse(SubwayStation subwayStation) {
        this.id = subwayStation.getId();
        this.name = subwayStation.getStationName();
        this.line = subwayStation.getLine().getName();
        this.timeMinSec = subwayStation.getTimeMinSec();
        this.acmlTime = subwayStation.getAccumulateTime();
        this.distKm = subwayStation.getDistance();
        this.acmlDist = subwayStation.getAccumulateDistance();
    }
}

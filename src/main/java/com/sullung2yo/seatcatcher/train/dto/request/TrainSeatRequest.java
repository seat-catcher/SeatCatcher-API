package com.sullung2yo.seatcatcher.train.dto.request;

import com.sullung2yo.seatcatcher.train.domain.SeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "좌석에 대한 DTO입니다.")
public class TrainSeatRequest {

    @Schema(description = "좌석 위치 정보입니다. 오른쪽 위부터 0으로 시작합니다.")
    private Integer seatLocation;

    @Schema(description = "좌석 타입입니다. 일반석, 노약좌석, 임산부석 등이 있습니다.")
    private SeatType seatType;

    @Schema(description = "다른 사람들이 해당 좌석을 찜한 수입니다.")
    private Integer jjimCount;

}

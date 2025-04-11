package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.train.domain.SeatType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "어떤 좌석 자체에 대한 정보를 담은 DTO입니다.")
public class TrainSeatResponse {
    @Schema(description = "좌석의 ID입니다. Primary Key 로 사용됩니다.")
    private Long id;

    @Schema(description = "좌석의 위치 정보입니다. 우측 상단부터 0입니다.")
    private int seatLocation;

    @Schema(description = "좌석의 타입입니다. 일반석, 노약자석, 임산부석 등의 종류가 있습니다.")
    private SeatType seatType;

    @Schema(description = "유저들이 해당 좌석을 찜한 횟수입니다.")
    private int jjimCount;

    public TrainSeatResponse(TrainSeat record) {
        this.id = record.getId();
        this.seatLocation = record.getSeatLocation();
        this.seatType = record.getSeatType();
        this.jjimCount = record.getJjimCount();
    }
}

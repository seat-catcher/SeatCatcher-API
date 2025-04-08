package com.sullung2yo.seatcatcher.train.dto.request;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SeatGroup에 대한 Request DTO입니다.")
public class TrainSeatGroupRequest {
    @Schema(description = "Seat Group의 타입입니다.")
    private SeatGroupType type;

    public TrainSeatGroupRequest(TrainSeatGroup trainSeatGroup) {
        this.type = trainSeatGroup.getType();
    }
}

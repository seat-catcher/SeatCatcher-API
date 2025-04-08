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
@Schema(description = "SeatGroup's request DTO")
public class TrainSeatGroupRequest {
    @Schema(description = "Seat Group's Type.")
    private SeatGroupType type;

    public TrainSeatGroupRequest(TrainSeatGroup trainSeatGroup) {
        this.type = trainSeatGroup.getType();
    }
}

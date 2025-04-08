package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "SeatGroup's response DTO")
public class TrainSeatGroupResponse {
    @Schema(description = "Seat Group's Id.")
    private Long id;

    @Schema(description = "Seat Group's Type.")
    private SeatGroupType groupType;

    public TrainSeatGroupResponse(TrainSeatGroup trainSeatGroup) {
        this.id = trainSeatGroup.getId();
        this.groupType = trainSeatGroup.getType();
    }
}

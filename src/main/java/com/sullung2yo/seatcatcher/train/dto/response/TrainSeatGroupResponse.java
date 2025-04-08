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
@Schema(description = "SeatGroup에 대한 Response DTO입니다.")
public class TrainSeatGroupResponse {
    @Schema(description = "Seat Group의 ID이며, Primary key 입니다.")
    private Long id;

    @Schema(description = "Seat Group의 타입입니다.")
    private SeatGroupType groupType;

    public TrainSeatGroupResponse(TrainSeatGroup trainSeatGroup) {
        this.id = trainSeatGroup.getId();
        this.groupType = trainSeatGroup.getType();
    }
}

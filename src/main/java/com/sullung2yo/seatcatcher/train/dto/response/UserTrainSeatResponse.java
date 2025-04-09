package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "착석 정보에 대한 Response DTO입니다.")
public class UserTrainSeatResponse {
    @Schema(description = "착석 정보의 ID입니다. Primary Key 로 사용됩니다.")
    private Long id;

    @Schema(description = "자리에 앉을 유저의 ID입니다.")
    private Long userId;

    @Schema(description = "유저가 앉을 자리의 ID입니다.")
    private Long seatId;

    public UserTrainSeatResponse(UserTrainSeat userTrainSeat) {
        this.id = userTrainSeat.getId();
        this.userId = userTrainSeat.getUser().getId();
        this.seatId = userTrainSeat.getTrainSeat().getId();
    }
}

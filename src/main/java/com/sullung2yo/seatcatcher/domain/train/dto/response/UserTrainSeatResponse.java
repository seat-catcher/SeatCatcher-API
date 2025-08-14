package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.train.entity.UserTrainSeat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "착석 정보에 대한 Response DTO입니다.")
public class UserTrainSeatResponse {
    @Schema(description = "착석 정보의 ID입니다. Primary Key 로 사용됩니다.")
    private Long id;

    @Schema(description = "자리에 앉을 유저의 ID입니다.")
    @NotNull(message = "사용자 ID는 필수입니다!")
    private Long userId;

    @Schema(description = "유저가 앉을 자리의 ID입니다.")
    @NotNull(message = "좌석 ID는 필수입니다!")
    private Long seatId;

    public UserTrainSeatResponse(UserTrainSeat userTrainSeat) {
        this.id = userTrainSeat.getId();

        if(userTrainSeat.getUser() == null) throw new IllegalArgumentException("착석 정보에 유저는 비어있을 수 없습니다.");
        if(userTrainSeat.getTrainSeat() == null) throw new IllegalArgumentException("착석 정보에 좌석은 비어있을 수 없습니다.");

        this.userId = userTrainSeat.getUser().getId();
        this.seatId = userTrainSeat.getTrainSeat().getId();
    }
}

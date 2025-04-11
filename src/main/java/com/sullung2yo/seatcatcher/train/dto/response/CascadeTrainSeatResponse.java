package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "어떤 그룹에 속한 좌석들에 대한 기본 정보, 거기에 앉은 유저 정보, 유저의 현재 이용중인 경로까지 다 담고 있을 DTO입니다.")
public class CascadeTrainSeatResponse extends TrainSeatResponse {

    /*

    @Schema(description = "해당 좌석에 착석해 있는 유저에 대한 DTO입니다.")
    private UserResponse user;

    */ //TODO :: UserDTO 가 정의되면 들어가야 합니다.

    /*

    @Schema(description = "자리에 앉은 유저가 현재 사용중인 경로 정보에 대한 DTO입니다.")
    private PathHistoryResponse path;

    */ //TODO :: PathHistoryDTO 가 정의되면 들어가야 합니다.

    public CascadeTrainSeatResponse(TrainSeat trainSeat) {
        super(trainSeat);

        // 나머지도 세팅해줘야 합니다.
    }
}

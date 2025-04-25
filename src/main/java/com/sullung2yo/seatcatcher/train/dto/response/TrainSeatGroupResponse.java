package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.Train;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Schema(description = "SeatGroup에 대한 Response DTO입니다.")
public class TrainSeatGroupResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SingleResponse
    {
        @Schema(description = "Seat Group의 ID이며, Primary key 입니다.")
        private Long id;

        @Schema(description = "Seat Group이 속한 열차의 번호입니다.")
        private String trainCode;

        @Schema(description = "Seat Group이 속한 차량의 번호입니다.")
        private String carCode;

        @Schema(description = "Seat Group의 타입입니다.")
        private SeatGroupType groupType;

        public SingleResponse(Train record) {
            this.id = record.getId();
            this.trainCode = record.getTrainCode();
            this.carCode = record.getCarCode();
            this.groupType = record.getType();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ResponseList
    {
        @Schema(description = "Seat Group 응답 모음집입니다.")
        private List<SingleResponse> items;

        public ResponseList()
        {
            items = new ArrayList<>();
        }
    }
}

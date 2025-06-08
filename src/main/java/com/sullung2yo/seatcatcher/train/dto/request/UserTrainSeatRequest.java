package com.sullung2yo.seatcatcher.train.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "착석 정보에 대한 Request DTO입니다.")
public class UserTrainSeatRequest {

    @Schema(description = "유저가 앉을 자리의 ID입니다.")
    private Long seatId;

    @Schema(description = "유저 간에 오고 가는 크레딧 양입니다.")
    private Long creditAmount;
}

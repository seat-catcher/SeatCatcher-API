package com.sullung2yo.seatcatcher.train.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "착석 정보에 대한 Request DTO입니다.")
public class UserTrainSeatRequest {

    @Schema(description = "자리에 앉을 유저의 ID입니다.")
    private Long userId;

    @Schema(description = "유저가 앉을 자리의 ID입니다.")
    private Long seatId;
    
}

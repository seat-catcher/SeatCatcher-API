package com.sullung2yo.seatcatcher.train.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated // DTO 요구사항 만족 안하면 400 에러 발생시켜주는 어노테이션
@Schema(description = "착석 정보에 대한 Request DTO입니다.")
public class UserTrainSeatRequest {

    @NotNull
    @Schema(description = "유저가 앉을 자리의 ID입니다.")
    private Long seatId;

    @NotNull
    @Schema(description = "유저 간에 오고 가는 크레딧 양입니다.")
    private Long creditAmount;
    
}

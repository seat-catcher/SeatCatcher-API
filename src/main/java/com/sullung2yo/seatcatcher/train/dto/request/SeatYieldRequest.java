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
public class SeatYieldRequest {

    @NotNull
    @Schema(description = "좌석 id입니다.")
    private Long seatId;

    @NotNull
    @Schema(description = "상대방 유저의 id입니다.")
    private Long takerId;

    // 자신의 uid 는 토큰에서 추출하므로 필요 없음.
}

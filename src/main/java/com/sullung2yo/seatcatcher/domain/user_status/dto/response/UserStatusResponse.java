package com.sullung2yo.seatcatcher.domain.user_status.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "유저 상태값 Response DTO")
public class UserStatusResponse {

    @Schema(description = "유저가 타고 있던 열차 코드")
    private String trainCode;

    @Schema(description = "유저가 타고 있던 차량 번호")
    private String carCode;

    @Schema(description = "유저가 타고 있던 좌석 그룹")
    private String seatSection;

    @Schema(description = "유저가 양보를 요청했던 좌석의 ID (구독 유지를 위함)")
    private Long seatIdRequested;

}

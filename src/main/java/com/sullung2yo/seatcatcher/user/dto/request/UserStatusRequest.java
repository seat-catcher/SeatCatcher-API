package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "유저 상태값 Request DTO")
public class UserStatusRequest {

    @Schema(description = "유저가 타고 있던 열차 코드", example = "7134")
    private String trainCode;

    @Schema(description = "유저가 타고 있던 차량 번호", example = "7005")
    private String carCode;

    @Schema(description = "유저가 타고 있던 좌석 그룹", example = "프론트에서 저장하고 있는 구역 이름")
    private String seatSection;

    @Schema(description = "유저가 양보를 요청했던 좌석의 ID (구독 유지를 위함)", example = "123")
    private Long seatIdRequested;

}

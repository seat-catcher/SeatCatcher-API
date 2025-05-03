package com.sullung2yo.seatcatcher.train.dto.request;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
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
public class GetSittingInfoRequest {

    @NotNull
    @Schema(description = "지하철 ID", example = "1001")
    private String trainCode; // 지하철 코드

//    @NotNull
//    @Schema(description = "차량 ID", example = "1001")
//    private String carCode; // 차량 코드
//
//    @NotNull
//    @Schema(description = "좌석 그룹 타입", example = "ELDERY_A, ELDERY_B, NORMAL_A_14, NORMAL_B_14, NORMAL_C_14, NORMAL_A_12, NORMAL_B_12, NORMAL_C_12")
//    private SeatGroupType seatGroupType; // 좌석 그룹 타입

}

package com.sullung2yo.seatcatcher.subway_station.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StartJourneyRequest {

    @Schema(description = "유저가 현재 탑승한 열차의 Code", example = "7102")
    @NotBlank(message = "열차 코드는 필수로 입력해야 합니다!")
    private String trainCode;

}

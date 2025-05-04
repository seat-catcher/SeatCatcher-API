package com.sullung2yo.seatcatcher.train.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ToString
public class SeatInfoRequest {

    @NotNull
    private String trainCode; // 기차 코드

    @NotNull
    private String carCode; // 차량 코드
}

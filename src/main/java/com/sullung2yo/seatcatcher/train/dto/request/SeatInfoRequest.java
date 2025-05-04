package com.sullung2yo.seatcatcher.train.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Valid
public class SeatInfoRequest {

    @NotNull
    private String trainCode; // 기차 코드

    @NotNull
    private String carCode; // 차량 코드
}

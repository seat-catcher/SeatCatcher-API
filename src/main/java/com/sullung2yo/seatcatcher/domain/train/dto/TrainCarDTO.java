package com.sullung2yo.seatcatcher.domain.train.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainCarDTO {

    private String trainCode;
    private String carCode;
}

package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.train.enums.SeatGroupType;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class SeatInfoResponse {
    private String trainCode;
    private String carCode;
    private SeatGroupType seatGroupType;
    private List<SeatStatus> seatStatus;
}

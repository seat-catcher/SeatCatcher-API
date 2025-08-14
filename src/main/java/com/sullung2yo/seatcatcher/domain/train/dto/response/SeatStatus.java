package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.train.enums.SeatType;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class SeatStatus {
    private Long seatId;
    private Integer seatLocation;
    private SeatType seatType;
    private SeatOccupant occupant;
}
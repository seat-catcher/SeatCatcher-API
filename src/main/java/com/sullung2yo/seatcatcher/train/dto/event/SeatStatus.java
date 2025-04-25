package com.sullung2yo.seatcatcher.train.dto.event;

import com.sullung2yo.seatcatcher.train.domain.SeatOccupationStatus;
import com.sullung2yo.seatcatcher.train.domain.SeatType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SeatStatus {
    private Long seatId; // 좌석 ID
    private Integer seatLocation; // 좌석 위치 (0 .. 14 또는 0 .. 12, ...)
    private SeatType seatType; // 좌석 타입 (일반석, 노약자석, ...)
    private SeatOccupationStatus isOccupied; // 좌석 점유 여부 (true: 점유, false: 비어있음)
    private SeatOccupant occupant; // 좌석 점유자 정보
}

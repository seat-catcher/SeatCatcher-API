package com.sullung2yo.seatcatcher.train.dto.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SeatEvent {
    private String trainCode; // 기차 코드
    private String carCode; // 차량 코드
    private List<SeatStatus> seatStatus; // 좌석 상태 리스트
}

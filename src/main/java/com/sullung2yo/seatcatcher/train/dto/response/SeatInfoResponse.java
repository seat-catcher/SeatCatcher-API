package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class SeatInfoResponse {
    private String trainCode; // 기차 코드
    private String carCode; // 차량 코드
    private SeatGroupType seatGroupType; // 좌석 그룹 타입
    private List<SeatStatus> seatStatus; // 좌석 상태 리스트
}

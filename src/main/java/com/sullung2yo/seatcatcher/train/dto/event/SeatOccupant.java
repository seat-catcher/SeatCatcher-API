package com.sullung2yo.seatcatcher.train.dto.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatOccupant {
    private Long userId; // 사용자 ID
    private String nickname; // 사용자 닉네임
    private String getOffRemainingCount; // 도착역까지 얼마나 남았는지
}

package com.sullung2yo.seatcatcher.train.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SeatOccupant {
    private Long userId; // 사용자 ID
    private String nickname; // 사용자 닉네임
    private Integer getOffRemainingCount; // 도착역까지 얼마나 남았는지
}

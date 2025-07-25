package com.sullung2yo.seatcatcher.domain.train.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeatGroupType {
    NORMAL_A_14(14), // 일반 구역 A. 좌석 개수 14개
    NORMAL_B_14(14), // 일반 구역 B. 좌석 개수 14개
    NORMAL_C_14(14), // 일반 구역 C. 좌석 개수 14개

    NORMAL_A_12(12), // 일반 구역 A. 좌석 개수 12개
    NORMAL_B_12(12), // 일반 구역 B. 좌석 개수 12개
    NORMAL_C_12(12), // 일반 구역 C. 좌석 개수 12개

    PRIORITY_A(6), // 교통약자석 A. 좌석 개수 6개
    PRIORITY_B(6), // 교통약자석 B. 좌석 개수 6개

    // OLD
    ELDERLY_A(6), // 노약자구역 A. 좌석 개수 6개
    ELDERLY_B(6); // 노약자구역 B. 좌석 개수 6개

    private final int seatCount;
}

package com.sullung2yo.seatcatcher.user.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreditPolicy {

    CREDIT_FOR_SIT_INFO_PROVIDE(300); // 좌석 정보를 제공하기 위한 최소 크레딧 설정
    //CREDIT_FOR_SEAT_YIELD_REQUEST(300), // 양보 요청 시 차감 크레딧
    //CREDIT_FOR_SEAT_YIELD_APPROVE(300); // 양보 요청 수락 시 제공 크레딧

    private final long credit;
}

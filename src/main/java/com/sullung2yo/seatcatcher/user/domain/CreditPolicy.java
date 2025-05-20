package com.sullung2yo.seatcatcher.user.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreditPolicy {

    CREDIT_FOR_SIT_INFO_PROVIDE(300);

    private final int credit;
}

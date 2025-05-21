package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;

public interface CreditService {
    // 크레딧 증감 메서드
    void creditModification(Long userId, long amount, boolean isAddition, YieldRequestType yieldRequestType);
}

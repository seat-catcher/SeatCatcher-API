package com.sullung2yo.seatcatcher.domain.credit.service;

import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;
import com.sullung2yo.seatcatcher.user.domain.User;

public interface CreditService {
    // 크레딧 증감 처리 메서드
    void creditModification(Long userId, long amount, boolean isAddition, YieldRequestType yieldRequestType);

    // 크레딧 업데이트 메서드
    User applyCreditChange(User user, long creditToUpdate);
}

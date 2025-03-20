package com.sullung2yo.seatcatcher.user.dto.request;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AuthRequest {
    private Provider provider; // 소셜 로그인 공급자 (Apple, Kakao, Local)
}

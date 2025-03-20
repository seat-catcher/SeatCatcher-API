package com.sullung2yo.seatcatcher.user.dto.request;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoAuthRequest extends AuthRequest {
    private String accessToken;

    public KakaoAuthRequest() {
        setProvider(Provider.KAKAO);
    }
}

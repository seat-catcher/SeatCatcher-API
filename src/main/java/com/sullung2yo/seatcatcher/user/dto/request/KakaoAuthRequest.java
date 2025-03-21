package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 카카오 인증에 필요한 accessToken DTO 클래스
 */
@Getter
@Setter
public class KakaoAuthRequest{
    /**
     * 카카오 Auth server에서 제공받은 accessToken
     */
    private String accessToken;
}

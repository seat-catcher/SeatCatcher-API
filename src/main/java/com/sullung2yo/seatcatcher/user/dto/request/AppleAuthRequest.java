package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Apple 인증에 필요한 identityToken DTO 클래스
 */
@Getter
@Setter
public class AppleAuthRequest {
    /**
     * Apple Auth server에서 제공받은 identityToken
     */
    private String identityToken;
}

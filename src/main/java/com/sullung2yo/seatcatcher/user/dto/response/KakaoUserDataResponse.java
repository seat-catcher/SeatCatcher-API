package com.sullung2yo.seatcatcher.user.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserDataResponse {
    private String id; // Kakao user id
    private KakaoAccount kakaoAccount; // Kakao account info

    @Getter
    public static class KakaoAccount {
        /**
         * Reference
         * <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#kakaoaccount">...</a>
         */
        private String email;
    }
}

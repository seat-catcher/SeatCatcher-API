package com.sullung2yo.seatcatcher.jwt.provider;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;

import java.util.List;
import java.util.Map;

public interface TokenProvider {

    /**
     * 주어진 이메일, 추가 데이터, 및 토큰 타입을 기반으로 토큰 문자열을 생성한다.
     *
     * 이 메소드는 사용자 인증 및 권한 부여 과정에서 사용될 토큰을 생성하기 위해 호출된다.
     *
     * @param email 사용자의 이메일 주소
     * @param payload 토큰에 포함될 추가 데이터
     * @param tokenType 생성할 토큰의 유형
     * @return 생성된 토큰 문자열
     */
    public String createToken(String email, Map<String, ?> payload, TokenType tokenType);
    public List<String> refreshToken(String refreshToken);

}

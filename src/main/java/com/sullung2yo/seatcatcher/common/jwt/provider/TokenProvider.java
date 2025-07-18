package com.sullung2yo.seatcatcher.common.jwt.provider;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

public interface TokenProvider {

    /**
     * 주어진 이메일, 추가 데이터, 및 토큰 타입을 기반으로 토큰 문자열을 생성한다.
     *
     * 이 메소드는 사용자 인증 및 권한 부여 과정에서 사용될 토큰을 생성하기 위해 호출된다.
     *
     * @param subject 토큰의 주체(subject)로 사용될 사용자 식별자
     * @param payload 토큰에 포함될 추가 데이터
     * @param tokenType 생성할 토큰의 유형
     * @return 생성된 토큰 문자열
     */
    public String createToken(String subject, Map<String, ?> payload, TokenType tokenType);
    public List<String> refreshToken(String refreshToken);
    public String getProviderIdFromToken(String token);

    /**
     * WebSocket 연결 시 주어진 토큰을 검증하고,
     * 유효한 경우에는 해당 토큰에 포함된 사용자 정보를 기반으로 Authentication 객체를 반환하는 함수
     * 반환한 Authentication 객체는 WebSocket 세션 인증에 사용됩니다.
     * @param token 검증할 Access Token
     * @return 유효한 토큰에 대한 Authentication 객체
     */
    Authentication getAuthenticationForWebSocket(String token);
}

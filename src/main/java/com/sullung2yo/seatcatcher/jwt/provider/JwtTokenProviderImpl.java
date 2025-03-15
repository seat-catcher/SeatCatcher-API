package com.sullung2yo.seatcatcher.jwt.provider;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
@NoArgsConstructor
public class JwtTokenProviderImpl implements TokenProvider {

    @Getter
    private SecretKey secretKey;
    private long accessTokenValidMilliseconds;
    private long refreshTokenValidMilliseconds;

    /**
     * 주어진 JWT 비밀 문자열과 토큰 유효 기간을 사용하여 JwtTokenProviderImpl 인스턴스를 초기화합니다.
     * jwt secret 을 이용해 JWT 서명에 사용할 SecretKey 객체를 생성하며,
     * access 토큰과 refresh 토큰에 각각 적용될 유효 기간(밀리초 단위)을 설정합니다.
     *
     * @param secret JWT 서명에 필요한 비밀 문자열.
     * @param accessTokenValidMilliseconds access 토큰의 유효 기간 (밀리초 단위).
     * @param refreshTokenValidMilliseconds refresh 토큰의 유효 기간 (밀리초 단위).
     */
    public JwtTokenProviderImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-valid-millisecond}") long accessTokenValidMilliseconds,
            @Value("${jwt.refresh-token-valid-millisecond}") long refreshTokenValidMilliseconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidMilliseconds = accessTokenValidMilliseconds;
        this.refreshTokenValidMilliseconds = refreshTokenValidMilliseconds;
    }

    /**
     * 주어진 이메일과 추가 클레임 정보를 바탕으로 JWT 토큰을 생성한다.
     * 입력된 토큰 타입에 따라 액세스 토큰과 리프레시 토큰의 유효 기간을 적용하며,
     * 유효하지 않은 토큰 타입이 전달되면 예외를 발생시킨다.
     *
     * @param email 사용자의 이메일 주소로, 토큰의 주체(subject)로 사용된다.
     * @param payload JWT 토큰에 포함할 추가 정보를 담은 맵
     * @param tokenType 생성할 토큰의 타입 (ACCESS 또는 REFRESH)
     * @return 생성된 JWT 토큰 문자열
     * @throws IllegalArgumentException 유효하지 않은 토큰 타입인 경우
     */
    @Override
    public String createToken(String email, Map<String, ?> payload, TokenType tokenType) {
        Claims claims;

        if (tokenType == TokenType.ACCESS) {
            claims = generateClaims(email, payload, accessTokenValidMilliseconds);
        } else if (tokenType == TokenType.REFRESH) {
            claims = generateClaims(email, payload, refreshTokenValidMilliseconds);
        } else {
            throw new IllegalArgumentException("유효하지 않은 토큰 타입입니다: " + tokenType);
        }

        return Jwts.builder()
                .signWith(secretKey)
                .claims(claims)
                .compact();
    }

    /**
     * 주어진 주체와 추가 페이로드, 유효 기간을 바탕으로 JWT 생성을 위한 클레임(Claims) 객체를 생성합니다.
     * 클레임 객체에는 토큰의 주체, 발행 시각 및 만료 시각이 포함되며, 제공된 추가 데이터가 병합됩니다.
     *
     * @param subject JWT의 주체로, 일반적으로 사용자 이메일 또는 식별자를 나타냅니다.
     * @param payload JWT 클레임에 추가될 사용자 정의 데이터
     * @param tokenValidMilliseconds 토큰의 유효 기간(밀리초 단위)
     * @return JWT 생성을 위한 클레임 객체
     */
    private Claims generateClaims(String subject, Map<String, ?> payload, long tokenValidMilliseconds) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + tokenValidMilliseconds);

        return Jwts.claims()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiredAt)
                .add(payload)
                .build();
    }
}

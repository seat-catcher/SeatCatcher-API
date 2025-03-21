package com.sullung2yo.seatcatcher.jwt.provider;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProviderImpl implements TokenProvider {

    @Getter
    private SecretKey secretKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-valid-millisecond}")
    private long accessTokenValidMilliseconds;

    @Value("${jwt.refresh-token-valid-millisecond}")
    private long refreshTokenValidMilliseconds;

    @PostConstruct // Value로 의존성 주입이 이루어진 후 secretKey 초기화 작업 수행
    public void initSecretKey(){
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 주어진 이메일과 추가 클레임 정보를 바탕으로 JWT 토큰을 생성한다.
     * 입력된 토큰 타입에 따라 액세스 토큰과 리프레시 토큰의 유효 기간을 적용하며,
     * 유효하지 않은 토큰 타입이 전달되면 예외를 발생시킨다.
     *
     * @param providerId 외부 인증 서버에서 전달받은 사용자 Identifier, 토큰의 주체(subject)로 사용된다.
     * @param payload JWT 토큰에 포함할 추가 정보를 담은 맵
     * @param tokenType 생성할 토큰의 타입 (ACCESS 또는 REFRESH)
     * @return 생성된 JWT 토큰 문자열
     * @throws IllegalArgumentException 유효하지 않은 토큰 타입인 경우
     */
    @Override
    public String createToken(String providerId, Map<String, ?> payload, TokenType tokenType) {

        Claims claims;

        if (tokenType == TokenType.ACCESS) {
            claims = generateClaims(providerId, payload, accessTokenValidMilliseconds);
        } else if (tokenType == TokenType.REFRESH) {
            claims = generateClaims(providerId, payload, refreshTokenValidMilliseconds);
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
        log.debug("토큰 생성: subject={}, payload={}, 유효 기간={}ms", subject, payload, tokenValidMilliseconds);

        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + tokenValidMilliseconds);
        log.debug("토큰 유효 기간: {}", expiredAt);

        return Jwts.claims()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiredAt)
                .add(payload)
                .build();
    }
}

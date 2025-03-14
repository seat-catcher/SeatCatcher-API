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

    public JwtTokenProviderImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-valid-millisecond}") long accessTokenValidMilliseconds,
            @Value("${jwt.refresh-token-valid-millisecond}") long refreshTokenValidMilliseconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidMilliseconds = accessTokenValidMilliseconds;
        this.refreshTokenValidMilliseconds = refreshTokenValidMilliseconds;
    }

    @Override
    public String createToken(String email, Map<String, ?> payload, TokenType tokenType) {
        Claims claims;

        if (tokenType == TokenType.ACCESS) {
            claims = generateClaims(email, payload, accessTokenValidMilliseconds);
        } else if (tokenType == TokenType.REFRESH) {
            claims = generateClaims(email, payload, refreshTokenValidMilliseconds);
        }
        else {
            throw new RuntimeException("Invalid token type");
        }

        return Jwts.builder()
                .signWith(secretKey)
                .claims(claims)
                .compact();
    }

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

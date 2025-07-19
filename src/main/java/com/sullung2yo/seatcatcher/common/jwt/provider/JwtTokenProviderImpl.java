package com.sullung2yo.seatcatcher.common.jwt.provider;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.domain.auth.entity.RefreshToken;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user.domain.UserRole;
import com.sullung2yo.seatcatcher.domain.auth.repository.RefreshTokenRepository;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
public class JwtTokenProviderImpl implements TokenProvider {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Getter
    private SecretKey secretKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-valid-millisecond}")
    private long accessTokenValidMilliseconds;

    @Value("${jwt.refresh-token-valid-millisecond}")
    private long refreshTokenValidMilliseconds;

    public JwtTokenProviderImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

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
            claims = generateClaims(providerId, payload, TokenType.ACCESS);
        } else if (tokenType == TokenType.REFRESH) {
            claims = generateClaims(providerId, payload, TokenType.REFRESH);
        } else {
            log.error("Unsupported token type: {}", tokenType);
            throw new TokenException("유효하지 않은 토큰 타입입니다: " + tokenType, ErrorCode.INVALID_TOKEN);
        }

        String token = Jwts.builder().signWith(secretKey).claims(claims).compact();
        if (tokenType == TokenType.REFRESH) {
            // RefreshToken은 DB에 저장
            Optional<User> userOptional = userRepository.findByProviderId(providerId);
            if (userOptional.isPresent()) {
                RefreshToken refreshToken = RefreshToken.builder()
                        .user(userOptional.get())
                        .refreshToken(token)
                        .expiredAt(claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                        .build();
                refreshTokenRepository.save(refreshToken);
            } else {
                log.error("{}인 사용자를 찾을 수 없습니다.", providerId);
                throw new TokenException("토큰에 담긴 사용자 정보가 유효하지 않습니다.", ErrorCode.USER_NOT_FOUND);
            }
        }
        return token;
    }

    @Override
    public List<String> refreshToken(String refreshToken) {
        // 1. refreshToken 유효성 검증
        if (!validateToken(refreshToken, TokenType.REFRESH)) {
            log.error("Invalid refresh token: {}", refreshToken);
            throw new TokenException("Refresh 토큰이 유효하지 않습니다.", ErrorCode.INVALID_TOKEN);
        }

        // 2. refreshToken에서 providerId 추출
        String providerId = getProviderIdFromToken(refreshToken);

        // 3. providerId에 해당하는 사용자 탐색
        Optional<User> userOptional = userRepository.findByProviderId(providerId);
        if (userOptional.isEmpty()) {
            log.error("User with id {} not found", providerId);
            throw new TokenException("토큰에 담긴 사용자 정보가 유효하지 않습니다.", ErrorCode.USER_NOT_FOUND);
        }

        // 4. accessToken 및 refreshToken 재발급 (Token Rotation)
        Claims accessClaims = generateClaims(providerId, null, TokenType.ACCESS);
        Claims refreshClaims = generateClaims(providerId, null, TokenType.REFRESH);

        String newAccessToken = Jwts.builder().signWith(secretKey).claims(accessClaims).compact();
        String newRefreshToken = Jwts.builder().signWith(secretKey).claims(refreshClaims).compact();

        // 5. RefreshToken DB에 저장
        // RefreshToken을 갱신한다는건, 로그인을 통해 이미 RefreshToken을 발급받아서 DB에 저장한 상태이므로
        // DB에 있는 RefreshToken값을 업데이트한다
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findRefreshTokenByUserAndRefreshToken(userOptional.get(), refreshToken);
        if (refreshTokenOptional.isPresent()) {
            RefreshToken existingRefreshToken = refreshTokenOptional.get();
            existingRefreshToken.setRefreshToken(newRefreshToken); // 새로운 RefreshToken 저장

            Jws<Claims> claimsJws = parseToken(newRefreshToken);
            Date expiredAt = claimsJws.getPayload().getExpiration();
            existingRefreshToken.setExpiredAt(expiredAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()); // 만료 시간 갱신

            refreshTokenRepository.save(existingRefreshToken);
        } else {
            log.error("Refresh token not found in database for user: {}", providerId);
            throw new TokenException("Refresh 토큰 데이터베이스 검증 실패", ErrorCode.TOKEN_NOT_FOUND);
        }

        return List.of(newAccessToken, newRefreshToken);
    }

    public Boolean validateToken(String token, TokenType tokenType) {
        try {
            // 1. 토큰에서 providerId 추출
            String providerId = getProviderIdFromToken(token);
            if (providerId.isEmpty()) {
                return false;
            }

            // 2. providerId에 해당하는 사용자 탐색
            Optional<User> user = userRepository.findByProviderId(providerId);
            if (user.isEmpty()) {
                return false;
            }

            // 3. refreshToken의 경우, RefreshToken 테이블 확인 및 만료 여부 확인
            if (tokenType.equals(TokenType.REFRESH)) {
                Optional<RefreshToken> foundedToken = refreshTokenRepository.findRefreshTokenByUserAndRefreshToken(user.get(), token);
                if (foundedToken.isEmpty() || foundedToken.get().isExpired()) {
                    return false;
                }
            }

            // 4. Token 만료 여부 확인
            Claims payload = getPayloadFromToken(token);
            Date expiration = payload.getExpiration();
            return expiration != null && !expiration.before(new Date());
        } catch (RuntimeException e) {
            // JWT 파싱 실패, 유효하지 않은 토큰 등.. 예외 발생 시 false 반환
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderIdFromToken(String token) {
        Claims payload = getPayloadFromToken(token); // JWT
        return payload.getSubject();
    }

    @Override
    public Authentication getAuthenticationForWebSocket(String token) {
        // JWT 토큰 검증
        String providerId = this.getProviderIdFromToken(token);
        log.debug("providerId: {}", providerId);

        // 사용자 조회
        Optional<User> optionalUser = userRepository.findByProviderId(providerId);
        if (optionalUser.isEmpty()) {
            log.error("{}에 해당하는 사용자를 찾을 수 없습니다.", providerId);
            throw new TokenException("토큰에 담긴 사용자 정보가 유효하지 않습니다.", ErrorCode.USER_NOT_FOUND);
        }

        // 권한 정보가 있다면 포함
        User user = optionalUser.get();
        List<GrantedAuthority> authorities = null;
        if (user.getRole().equals(UserRole.ROLE_ADMIN)) {
            log.debug("관리자 권한 부여");
            authorities = Collections.singletonList(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name()));
        } else {
            log.debug("일반 사용자 권한 부여");
            authorities = Collections.singletonList(new SimpleGrantedAuthority(UserRole.ROLE_USER.name()));
        }
        UserDetails principal = new org.springframework.security.core.userdetails.User(providerId, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * JWT 토큰에서 payload를 추출하는 함수
     * @param token JWT 토큰
     * @return Claims (JWT payload)
     */
    private Claims getPayloadFromToken(String token) {
        try {
            Jws<Claims> claimsJws = parseToken(token); // JWT 파싱
            return claimsJws.getPayload(); // JWT에서 payload 추출해서 반환
        } catch (JwtException e) {
            log.error("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw new TokenException("Invalid JWT token: " + e.getMessage(), ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT 토큰을 파싱하여 Claims 객체를 반환하는 함수
     * @param token JWT 토큰
     * @return Jws<Claims> (JWT 서명된 Claims)
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token); // 이렇게 파싱한다고 하네요!
    }

    private Claims generateClaims(String subject, Map<String, ?> payload, TokenType tokenType) {
        log.debug("토큰 생성: type={}, subject={}, payload={}", tokenType.name(), subject, payload);

        long tokenValidMilliseconds = 0L;
        if (tokenType.equals(TokenType.ACCESS)) {
            tokenValidMilliseconds = accessTokenValidMilliseconds;
        } else if (tokenType.equals(TokenType.REFRESH)) {
            tokenValidMilliseconds = refreshTokenValidMilliseconds;
        } else {
            throw new IllegalArgumentException("유효하지 않은 토큰 타입입니다: " + tokenType);
        }

        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + tokenValidMilliseconds);
        log.debug("토큰 유효 기간: {}", expiredAt);

        return Jwts.claims()
                .subject(subject) // subject로 providerId 사용
                .issuedAt(now) // 발급 시간
                .expiration(expiredAt) // 만료 시간
                .add(payload) // 추가 클레임 정보
                .build();
    }
}

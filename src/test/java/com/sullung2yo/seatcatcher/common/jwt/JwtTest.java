package com.sullung2yo.seatcatcher.common.jwt;

import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.entity.RefreshToken;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.domain.UserRole;
import com.sullung2yo.seatcatcher.domain.auth.repository.RefreshTokenRepository;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // 리플렉션을 사용하여 private 필드에 접근하기 위한 유틸리티!!

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtTest {
    // JwtTokenProviderImpl에 있는 refreshToken 메서드를 테스트하는 테스트 클래스

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private JwtTokenProviderImpl tokenProvider;

    private final String secret = "testSecretKeyForUnitTest0123456789";
    private final long accessTokenValidMilliseconds = 3600000;     // 1시간
    private final long refreshTokenValidMilliseconds = 604800000;  // 7일

    @BeforeEach
    void setUp() {
        // JwtTokenProviderImpl 생성 및 필드 주입
        tokenProvider = new JwtTokenProviderImpl(userRepository, refreshTokenRepository);
        ReflectionTestUtils.setField(tokenProvider, "secret", secret);
        ReflectionTestUtils.setField(tokenProvider, "accessTokenValidMilliseconds", accessTokenValidMilliseconds);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenValidMilliseconds", refreshTokenValidMilliseconds);

        // 시크릿 키 초기화 호출
        tokenProvider.initSecretKey();
    }

    @Test
    void testCreateAccessToken() throws Exception {
        // Given
        User testUser = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .role(UserRole.ROLE_USER)
                .name("TestUser")
                .build();

        // When
        String accessToken = tokenProvider.createToken(
                testUser.getProviderId(),
                Map.of("role", testUser.getRole().toString()),
                TokenType.ACCESS
        );

        // Then
        assertThat(accessToken).isNotNull();
    }

    @Test
    void testCreateRefreshToken() throws Exception {
        // Given
        User testUser = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .role(UserRole.ROLE_USER)
                .name("TestUser")
                .build();

        when(userRepository.findByProviderId(testUser.getProviderId())).thenReturn(Optional.of(testUser));

        // When
        String refreshToken = tokenProvider.createToken(
                testUser.getProviderId(),
                Map.of("role", testUser.getRole().toString()),
                TokenType.REFRESH
        );

        // Then
        assertThat(refreshToken).isNotNull();
    }

    @Test
    void testCreateInvalidTypeToken() throws Exception {
        // Given
        User testUser = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .role(UserRole.ROLE_USER)
                .name("TestUser")
                .build();

        // When, Then
        assertThatThrownBy(() -> tokenProvider.createToken(
                testUser.getProviderId(),
                Map.of("role", testUser.getRole().toString()),
                null)
        ).isInstanceOf(TokenException.class).hasMessageContaining("유효하지 않은 토큰 타입입니다");
    }

    @Test
    void testRefreshToken() throws Exception {
        // Given
        User testUser = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .role(UserRole.ROLE_USER)
                .name("TestUser")
                .build();

        when(userRepository.findByProviderId(testUser.getProviderId())).thenReturn(Optional.of(testUser));

        String refreshToken = tokenProvider.createToken(
                testUser.getProviderId(),
                Map.of("role", testUser.getRole().toString()),
                TokenType.REFRESH
        );

        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7); // 7일 후 만료된다고 가정
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(testUser)
                .refreshToken(refreshToken)
                .expiredAt(expiredAt)
                .build();

        // When
        when(refreshTokenRepository.findRefreshTokenByUserAndRefreshToken(testUser, refreshToken)).thenReturn(Optional.of(refreshTokenEntity));
        List<String> newTokens = tokenProvider.refreshToken(refreshToken);

        // Then
        assertThat(newTokens.size()).isEqualTo(2);
        assertThat(newTokens.get(0)).isNotNull();
        assertThat(newTokens.get(1)).isNotEqualTo(refreshToken);
    }

    @Test
    void testTokenValidation() throws Exception {
        // Given
        User testUser = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .role(UserRole.ROLE_USER)
                .name("TestUser")
                .build();

        String accessToken = tokenProvider.createToken(
                testUser.getProviderId(),
                Map.of("role", testUser.getRole().toString()),
                TokenType.ACCESS
        );

        // When
        when(userRepository.findByProviderId(testUser.getProviderId())).thenReturn(Optional.of(testUser));
        boolean isValid = tokenProvider.validateToken(accessToken, TokenType.ACCESS);

        // Then
        assertThat(isValid).isTrue();
    }
}

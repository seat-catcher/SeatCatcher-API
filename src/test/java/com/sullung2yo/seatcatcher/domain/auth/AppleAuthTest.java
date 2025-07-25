package com.sullung2yo.seatcatcher.domain.auth;

import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.enums.UserRole;
import com.sullung2yo.seatcatcher.domain.auth.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.auth.service.AuthServiceImpl;
import com.sullung2yo.seatcatcher.domain.user.utility.random.NameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppleAuthTest {

    @Mock
    private UserRepository userRepository; // user repository 의존 Mocking

    @Mock
    private JwtTokenProviderImpl jwtTokenProvider; // jwtTokenProvider 의존 Mocking

    @Mock
    private NameGenerator nameGenerator; // 랜덤 이름 생성기 Mocking

    @Mock
    private WebClient.Builder webClientBuilder; // webClient 사용해서 apple 서버와 통신, 테스트 환경에서는 불가 -> Mocking

    @Mock
    private WebClient webClient; // webClient 사용해서 apple 서버와 통신, 테스트 환경에서는 불가 -> Mocking

    @Mock
    private ResourceLoader resourceLoader; // 리소스 로더 Mocking

    private AuthServiceImpl authService; // 테스트 대상 인스턴스

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient); // webClientBuilder.build() 호출 시 Mocked webClient 반환하도록 설정
        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, webClientBuilder, nameGenerator, resourceLoader); // 테스트 대상 인스턴스 생성
        ReflectionTestUtils.setField(authService, "appleClientId", "com.example.app"); // 테스트 appleClientId 설정
    }

    /**
     * 새로운 사용자가 Apple 로그인 시도하는 경우 테스트
     *
     * @throws Exception 예외 발생 시 Exception
     */
    @Test
    void testAppleAuthentication_NewUser() throws Exception {
        // Given
        AppleAuthRequest request = new AppleAuthRequest();
        request.setIdentityToken("fake.apple.token");
        request.setFcmToken("fcmToken");
        request.setNonce("test-nonce");
        request.setAuthorizationCode("fake-auth-code");
        String appleUserId = "apple123";

        // When
        // Spying : 실제 객체를 감싸고, 특정 메서드만 가짜 동작을 하도록 설정하는 방법
        AuthServiceImpl authServiceSpy = spy(authService); // authService 인스턴스를 spy로 생성 (가짜 객체 생성)

        // Spying한 validateAppleIdentityToken 메서드 호출 시 테스트로 설정한 appleUserId 반환하도록 Mocking
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString(), anyString());

        // 새로운 사용자라고 가정 (null을 반환했다고 가정)
        when(userRepository.findByProviderId(appleUserId)).thenReturn(Optional.empty());

        // jwt 생성했다고 치고
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access_token");
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh_token");

        // 토큰이 스파이 객체에서 잘 담기는지 테스트
        List<String> tokens = authServiceSpy.authenticate(request, Provider.APPLE);

        // Then
        assertEquals(2, tokens.size());
        assertEquals("access_token", tokens.get(0));
        assertEquals("refresh_token", tokens.get(1));

        // 사용자 저장되었는지 확인
        verify(userRepository).save(argThat(user ->
                user.getProviderId().equals(appleUserId) &&
                        user.getProvider() == Provider.APPLE
        ));
    }

    @Test
    void testAppleAuthentication_ExistingUser() throws Exception {
        // Given
        AppleAuthRequest request = new AppleAuthRequest();
        request.setIdentityToken("fake.apple.token");
        request.setFcmToken("fcmToken");
        request.setNonce("test-nonce");
        request.setAuthorizationCode("fake-auth-code");
        String appleUserId = "apple123";
        LocalDateTime now = LocalDateTime.now();

        User existingUser = User.builder()
                .providerId(appleUserId)
                .provider(Provider.APPLE)
                .role(UserRole.ROLE_USER)
                .build();

        // authService Spying
        AuthServiceImpl authServiceSpy = spy(authService);

        // Spying한 validateAppleIdentityToken 메서드 호출 시 테스트로 설정한 appleUserId 반환하도록 Mocking
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString(), anyString());

        // 사용자 DB에서 찾았다고 가정
        when(userRepository.findByProviderId(appleUserId)).thenReturn(Optional.of(existingUser));

        // jwt 생성했다고 치고
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access_token");
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh_token");

        // When
        List<String> tokens = authServiceSpy.authenticate(request, Provider.APPLE);

        // Then
        assertEquals(2, tokens.size());
        assertEquals("access_token", tokens.get(0));
        assertEquals("refresh_token", tokens.get(1));

        // 사용자 정보 존재 및 로그인 정보 업데이트되었는지 확인
        verify(userRepository).save(argThat(user ->
                user.getProviderId().equals(appleUserId) &&
                        user.getProvider() == Provider.APPLE &&
                        user.getLastLoginAt().isAfter(now)
        ));
    }

    @Test
    void testAppleAuthentication_InvalidToken() {
        // Given
        AppleAuthRequest request = new AppleAuthRequest();
        request.setIdentityToken("invalid.token");
        request.setFcmToken("fcmToken");

        // When/Then
        Exception exception = assertThrows(TokenException.class, () ->
                authService.authenticate(request, Provider.APPLE));
        assertTrue(exception.getMessage().contains("Identity Token 파싱 실패"));
    }
}
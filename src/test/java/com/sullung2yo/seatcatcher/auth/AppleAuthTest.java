package com.sullung2yo.seatcatcher.auth;

import com.nimbusds.jwt.JWTParser;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppleAuthTest {

    @Mock
    private UserRepository userRepository; // userrepository 의존 Mocking

    @Mock
    private JwtTokenProviderImpl jwtTokenProvider; // jwtTokenProvider 의존 Mocking

    @Mock
    private WebClient.Builder webClientBuilder; // webClient 사용해서 apple 서버와 통신, 테스트 환경에서는 불가 -> Mocking

    @Mock
    private WebClient webClient; // webClient 사용해서 apple 서버와 통신, 테스트 환경에서는 불가 -> Mocking

    private AuthServiceImpl authService; // 테스트 대상 인스턴스

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient); // webClientBuilder.build() 호출 시 Mocked webClient 반환하도록 설정
        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, webClientBuilder); // 테스트 대상 인스턴스 생성
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
        String appleUserId = "apple123";

        // When
        // Spying : 실제 객체를 감싸고, 특정 메서드만 가짜 동작을 하도록 설정하는 방법
        AuthServiceImpl authServiceSpy = spy(authService); // authService 인스턴스를 spy로 생성 (가짜 객체 생성)

        // Spying한 validateAppleIdentityToken 메서드 호출 시 테스트로 설정한 appleUserId 반환하도록 설정 (실제 메서드에서는 Apple providerId 값)
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString());

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
        String appleUserId = "apple123";
        LocalDateTime now = LocalDateTime.now();

        User existingUser = User.builder()
                .providerId(appleUserId)
                .provider(Provider.APPLE)
                .role(UserRole.ROLE_USER)
                .build();

        // authService Spying
        AuthServiceImpl authServiceSpy = spy(authService);

        // Spying한 validateAppleIdentityToken 메서드 호출 시 테스트로 설정한 appleUserId 반환하도록 설정 (실제 메서드에서는 Apple providerId 값)
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString());

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

        // When/Then
        Exception exception = assertThrows(Exception.class, () ->
                authService.authenticate(request, Provider.APPLE));
        assertTrue(exception.getMessage().contains("애플 IdentityToken 검증 오류"));
    }
}
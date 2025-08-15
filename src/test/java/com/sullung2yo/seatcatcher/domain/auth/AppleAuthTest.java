package com.sullung2yo.seatcatcher.domain.auth;

import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.auth.service.AuthService;
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

    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProviderImpl jwtTokenProvider;
    @Mock private NameGenerator nameGenerator;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    @Mock private ResourceLoader resourceLoader;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, webClientBuilder, nameGenerator, resourceLoader);
        ReflectionTestUtils.setField(authService, "appleClientId", "com.example.app");
    }

    @Test
    void testAppleAuthentication_NewUser() throws Exception {
        // Given
        AppleAuthRequest request = AppleAuthRequest.builder()
                .identityToken("fake.apple.token")
                .fcmToken("fcmToken")
                .nonce("test-nonce")
                .authorizationCode("fake-auth-code")
                .build();
        String appleUserId = "apple123";

        // Spy는 구현체로!
        AuthServiceImpl authServiceSpy = spy((AuthServiceImpl) authService);
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString(), anyString());

        when(userRepository.findByProviderId(appleUserId)).thenReturn(Optional.empty());
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.ACCESS))).thenReturn("access_token");
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.REFRESH))).thenReturn("refresh_token");

        // When
        List<String> tokens = authServiceSpy.authenticate(request, Provider.APPLE);

        // Then
        assertEquals(2, tokens.size());
        assertEquals("access_token", tokens.get(0));
        assertEquals("refresh_token", tokens.get(1));
        verify(userRepository).save(argThat(user ->
                appleUserId.equals(user.getProviderId()) && user.getProvider() == Provider.APPLE
        ));
    }

    @Test
    void testAppleAuthentication_ExistingUser() throws Exception {
        // Given
        AppleAuthRequest request = AppleAuthRequest.builder()
                .identityToken("fake.apple.token")
                .fcmToken("fcmToken")
                .nonce("test-nonce")
                .authorizationCode("fake-auth-code")
                .build();
        String appleUserId = "apple123";
        LocalDateTime now = LocalDateTime.now();

        User existingUser = User.builder()
                .providerId(appleUserId)
                .provider(Provider.APPLE)
                .role(UserRole.ROLE_USER)
                .build();

        AuthServiceImpl authServiceSpy = spy((AuthServiceImpl) authService);
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString(), anyString());

        when(userRepository.findByProviderId(appleUserId)).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.ACCESS))).thenReturn("access_token");
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.REFRESH))).thenReturn("refresh_token");

        // When
        List<String> tokens = authServiceSpy.authenticate(request, Provider.APPLE);

        // Then
        assertEquals(2, tokens.size());
        assertEquals("access_token", tokens.get(0));
        assertEquals("refresh_token", tokens.get(1));
        verify(userRepository).save(argThat(user ->
                appleUserId.equals(user.getProviderId())
                        && user.getProvider() == Provider.APPLE
                        && user.getLastLoginAt().isAfter(now)
        ));
    }

    @Test
    void testAppleAuthentication_InvalidToken() {
        // Given
        AppleAuthRequest request = AppleAuthRequest.builder()
                .identityToken("invalid.token")
                .fcmToken("fcmToken")
                .build();

        // When/Then
        Exception exception = assertThrows(TokenException.class,
                () -> authService.authenticate(request, Provider.APPLE));
        assertTrue(exception.getMessage().contains("Identity Token 파싱 실패"));
    }
}
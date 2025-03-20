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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppleAuthTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProviderImpl jwtTokenProvider;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, webClientBuilder);
        ReflectionTestUtils.setField(authService, "appleClientId", "com.example.app");
    }

    @Test
    void testAppleAuthentication_NewUser() throws Exception {
        // Given
        AppleAuthRequest request = new AppleAuthRequest();
        request.setIdentityToken("fake.apple.token");
        String appleUserId = "apple123";

        // Create a spy of AuthServiceImpl
        AuthServiceImpl authServiceSpy = spy(authService);

        // Bypass the RSA verification by mocking the validateAppleIdentityToken method
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString());

        // When user is NOT found in repository
        when(userRepository.findByProviderId(appleUserId)).thenReturn(Optional.empty());

        // Mock token creation
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access_token");
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh_token");

        // When
        List<String> tokens = authServiceSpy.authenticate(request);

        // Then
        assertEquals(2, tokens.size());
        assertEquals("access_token", tokens.get(0));
        assertEquals("refresh_token", tokens.get(1));

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

        User existingUser = User.builder()
                .providerId(appleUserId)
                .provider(Provider.APPLE)
                .role(UserRole.ROLE_USER)
                .build();

        // Create a spy of AuthServiceImpl
        AuthServiceImpl authServiceSpy = spy(authService);

        // Bypass the RSA verification by mocking the validateAppleIdentityToken method
        doReturn(appleUserId).when(authServiceSpy).validateAppleIdentityToken(anyString());

        // When user IS found in repository
        when(userRepository.findByProviderId(appleUserId)).thenReturn(Optional.of(existingUser));

        // Mock token creation
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access_token");
        when(jwtTokenProvider.createToken(eq(appleUserId), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh_token");

        // When
        List<String> tokens = authServiceSpy.authenticate(request);

        // Then
        assertEquals(2, tokens.size());
        assertEquals("access_token", tokens.get(0));
        assertEquals("refresh_token", tokens.get(1));

        // Verify user was updated
        verify(userRepository).save(argThat(user ->
                user.getProviderId().equals(appleUserId) &&
                        user.getProvider() == Provider.APPLE
        ));
    }

    @Test
    void testAppleAuthentication_InvalidToken() {
        // Given
        AppleAuthRequest request = new AppleAuthRequest();
        request.setIdentityToken("invalid.token");

        try (MockedStatic<JWTParser> jwtParserMock = mockStatic(JWTParser.class)) {
            // Use text.ParseException which is actually thrown by JWTParser
            jwtParserMock.when(() -> JWTParser.parse(anyString()))
                    .thenThrow(new java.text.ParseException("Invalid token format", 0));

            // When/Then
            Exception exception = assertThrows(Exception.class, () ->
                    authService.authenticate(request));

            assertTrue(exception.getMessage().contains("Failed to verify Apple identity token"));
        }
    }
}
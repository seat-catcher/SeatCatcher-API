package com.sullung2yo.seatcatcher.auth;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.response.KakaoUserDataResponse;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KakaoAuthTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProviderImpl jwtTokenProvider;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Mono<KakaoUserDataResponse> monoResponse;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        // Setup WebClient mock chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Create service with mocked dependencies
        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, webClientBuilder);
    }

    @Test
    void testKakaoAuthentication_NewUser() throws Exception {
        // Given
        KakaoAuthRequest request = new KakaoAuthRequest();
        request.setAccessToken("fake-token");

        // Create mock Kakao response
        KakaoUserDataResponse kakaoResponse = new KakaoUserDataResponse();
        kakaoResponse.setId("kakao-123");

        // Mock WebClient response - this is the key fix
        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(kakaoResponse);

        // Mock repository (new user)
        when(userRepository.findByProviderId("kakao-123")).thenReturn(Optional.empty());

        // Mock JWT token generation
        when(jwtTokenProvider.createToken(eq("kakao-123"), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access-token");
        when(jwtTokenProvider.createToken(eq("kakao-123"), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh-token");

        // When
        List<String> tokens = authService.authenticate(request);

        // Then
        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("access-token", tokens.get(0));
        assertEquals("refresh-token", tokens.get(1));

        // Verify user was saved
        verify(userRepository).save(argThat(user ->
                user.getProviderId().equals("kakao-123") &&
                        user.getProvider() == Provider.KAKAO &&
                        user.getRole() == UserRole.ROLE_USER
        ));
    }

    @Test
    void testKakaoAuthentication_ExistingUser() throws Exception {
        // Given
        KakaoAuthRequest request = new KakaoAuthRequest();
        request.setAccessToken("fake-token");

        // Create mock Kakao response
        KakaoUserDataResponse kakaoResponse = new KakaoUserDataResponse();
        kakaoResponse.setId("kakao-123");

        // Create existing user
        User existingUser = User.builder()
                .providerId("kakao-123")
                .provider(Provider.KAKAO)
                .role(UserRole.ROLE_USER)
                .build();

        // Mock WebClient response
        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(kakaoResponse);

        // Mock repository (existing user)
        when(userRepository.findByProviderId("kakao-123")).thenReturn(Optional.of(existingUser));

        // Mock JWT token generation
        when(jwtTokenProvider.createToken(anyString(), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access-token");
        when(jwtTokenProvider.createToken(anyString(), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh-token");

        // When
        List<String> tokens = authService.authenticate(request);

        // Then
        assertNotNull(tokens);
        assertEquals(2, tokens.size());

        // Verify user was updated
        verify(userRepository).save(argThat(user -> user.getProviderId().equals("kakao-123")));
    }

    @Test
    void testKakaoAuthentication_ApiFailure() {
        // Given
        KakaoAuthRequest request = new KakaoAuthRequest();
        request.setAccessToken("invalid-token");

        // Mock WebClient to return empty mono with null block result
        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(null);

        // When/Then
        Exception exception = assertThrows(Exception.class, () -> {
            authService.authenticate(request);
        });
        assertEquals("Failed to get user information from Kakao", exception.getMessage());
    }
}
package com.sullung2yo.seatcatcher.domain.auth;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.auth.service.AuthService;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.enums.UserRole;
import com.sullung2yo.seatcatcher.domain.auth.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.domain.auth.dto.response.KakaoUserDataResponse;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.auth.service.AuthServiceImpl;
import com.sullung2yo.seatcatcher.domain.user.utility.random.NameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kakao 로그인 인증 로직에 대한 단위 테스트 클래스.
 * WebClient를 통한 실제 카카오 서버와 통신하는게 아니라, Mock 객체를 사용해 테스트한다.
 */
@ExtendWith(MockitoExtension.class)
class KakaoAuthTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProviderImpl jwtTokenProvider;
    @Mock private NameGenerator nameGenerator;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    @Mock private ResourceLoader resourceLoader;

    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;
    @Mock private Mono<KakaoUserDataResponse> monoResponse;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, webClientBuilder, nameGenerator, resourceLoader);
    }

    @Test
    void testKakaoAuthentication_NewUser() throws Exception {
        // Given
        KakaoAuthRequest request = KakaoAuthRequest.builder()
                .accessToken("fake-token")
                .build();

        KakaoUserDataResponse kakaoResponse = KakaoUserDataResponse.builder()
                .id("kakao-123")
                .build();

        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(kakaoResponse);

        when(userRepository.findByProviderId("kakao-123")).thenReturn(Optional.empty());
        when(jwtTokenProvider.createToken(eq("kakao-123"), isNull(), eq(TokenType.ACCESS))).thenReturn("access-token");
        when(jwtTokenProvider.createToken(eq("kakao-123"), isNull(), eq(TokenType.REFRESH))).thenReturn("refresh-token");

        // When
        List<String> tokens = authService.authenticate(request, Provider.KAKAO);

        // Then
        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("access-token", tokens.get(0));
        assertEquals("refresh-token", tokens.get(1));

        verify(userRepository).save(argThat(user ->
                user.getProviderId().equals("kakao-123")
                        && user.getProvider() == Provider.KAKAO
                        && user.getRole() == UserRole.ROLE_USER
        ));
    }

    @Test
    void testKakaoAuthentication_ExistingUser() throws Exception {
        // Given
        KakaoAuthRequest request = KakaoAuthRequest.builder()
                .accessToken("fake-token")
                .build();

        KakaoUserDataResponse kakaoResponse = KakaoUserDataResponse.builder()
                .id("kakao-123")
                .build();

        User existingUser = User.builder()
                .providerId("kakao-123")
                .provider(Provider.KAKAO)
                .role(UserRole.ROLE_USER)
                .build();

        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(kakaoResponse);

        when(userRepository.findByProviderId("kakao-123")).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.createToken(anyString(), isNull(), eq(TokenType.ACCESS))).thenReturn("access-token");
        when(jwtTokenProvider.createToken(anyString(), isNull(), eq(TokenType.REFRESH))).thenReturn("refresh-token");

        // When
        List<String> tokens = authService.authenticate(request, Provider.KAKAO);

        // Then
        assertNotNull(tokens);
        assertEquals(2, tokens.size());

        verify(userRepository).save(argThat(user -> user.getProviderId().equals("kakao-123")));
    }

    @Test
    void testKakaoAuthentication_ApiFailure() {
        // Given
        KakaoAuthRequest request = KakaoAuthRequest.builder()
                .accessToken("invalid-token")
                .build();

        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(null);

        // When/Then
        Exception exception = assertThrows(Exception.class, () -> authService.authenticate(request, Provider.KAKAO));
        assertEquals("카카오 서버에서 사용자 정보를 가져오는데 실패했습니다.", exception.getMessage());
    }
}
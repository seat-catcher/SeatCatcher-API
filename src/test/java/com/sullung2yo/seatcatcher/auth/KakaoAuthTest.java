package com.sullung2yo.seatcatcher.auth;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user.domain.UserRole;
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
@ExtendWith(MockitoExtension.class) // Mockito를 편리하게 사용하도록 설정하는 Annotation -> @Mock, @InjectMocks 등을 메서드마다 선언하지 않게 해준다.
class KakaoAuthTest {

    // Mock 필드 - 가짜 객체를 사용하기 위해 선언하는 필드

    @Mock
    private UserRepository userRepository;
    // 이 테스트 케이스는 UserRepository를 테스트하는게 아니라, 카카오 인증 자체에 대한 테스트를 수행하므로 실제 UserRepository를 사용하지 않고 Mock 객체를 사용함

    @Mock
    private JwtTokenProviderImpl jwtTokenProvider;
    // JWT 발급도 실제 작동할 필요 X -> Mocking

    @Mock
    private NameGenerator nameGenerator;

    @Mock
    private WebClient.Builder webClientBuilder;
    // 실제 코드에서 WebClient Builder를 사용하는데, 테스트 환경에서는 사용 불가 -> Mocking

    @Mock
    private WebClient webClient;
    // WebClient는 외부와 통신할 때 사용하는 객체이므로, 테스트 환경에서는 사용 불가 -> Mocking

    @Mock
    private ResourceLoader resourceLoader;
    // ResourceLoader Mocking

    // ====================Web Client 체이닝 메서드 호출에 필요한 다양한 타입 Mocking===========================
    // WebClient -> RequestHeadersUriSpec -> RequestHeadersSpec -> ResponseSpec -> Mono<KakaoUserDataResponse> 순으로 호출되므로
    // 각각의 Mock 객체를 생성하여 WebClient의 메서드 호출에 대한 반환값을 설정해준다.

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Mono<KakaoUserDataResponse> monoResponse;

    // =================================================================================================

    private AuthServiceImpl authService; // 실제 테스트 대상 객체

    /**
     * 각 테스트 메서드 실행 전(BeforeEach)에 공통으로 수행할 셋업 로직.
     * WebClient 체이닝을 Mock으로 연결해줌과 동시에, 테스트 대상 서비스 인스턴스를 생성한다.
     */
    @BeforeEach
    void setUp() {
        // 1. WebClient Builder Mocking
        when(webClientBuilder.build()).thenReturn(webClient);

        // 2. GET 요청 시 requestHeadersUriSpec을 반환하는 Mocking 설정
        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        // 3. REQUEST URI 설정 시 requestHeadersSpec을 반환하는 Mocking 설정
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);

        // 4. Header 설정 시 requestHeadersSpec을 반환하는 Mocking 설정
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);

        // 5. Response 시 responseSpec을 반환하는 Mocking 설정
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // authService 인스턴스 생성해야 하니까 Mocking WebClient + 필요한 의존성 주입
        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, webClientBuilder, nameGenerator, resourceLoader);
    }

    @Test
    void testKakaoAuthentication_NewUser() throws Exception {
        // Given
        KakaoAuthRequest request = new KakaoAuthRequest();
        request.setAccessToken("fake-token");

        // Create mock Kakao response - 카카오 인증 서버에서 "kakao-123" 반환했다고 가정
        KakaoUserDataResponse kakaoResponse = new KakaoUserDataResponse();
        kakaoResponse.setId("kakao-123");

        // Mock WebClient response
        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(kakaoResponse);

        // 실제로 DB에는 없을거니까 kakao-123 쿼리했을 때 반환되는게 없을거라고 가정
        when(userRepository.findByProviderId("kakao-123")).thenReturn(Optional.empty());

        // JWT 잘 생성했다고 가정
        when(jwtTokenProvider.createToken(eq("kakao-123"), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access-token");
        when(jwtTokenProvider.createToken(eq("kakao-123"), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh-token");

        // When
        List<String> tokens = authService.authenticate(request, Provider.KAKAO);

        // Then
        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        assertEquals("access-token", tokens.get(0));
        assertEquals("refresh-token", tokens.get(1));

        // 사용자 저장되었는지 테스트 (실제로 저장되는건 아님)
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

        // Create mock Kakao response - 카카오 인증 서버에서 "kakao-123" 반환했다고 가정
        KakaoUserDataResponse kakaoResponse = new KakaoUserDataResponse();
        kakaoResponse.setId("kakao-123");

        // 현재 DB에 이 사용자 존재한다고 가정
        User existingUser = User.builder()
                .providerId("kakao-123")
                .provider(Provider.KAKAO)
                .role(UserRole.ROLE_USER)
                .build();

        // Mock WebClient response
        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(kakaoResponse);

        // 실제 DB에 쿼리하는게 아니므로 existingUser 반환한다고 설정
        when(userRepository.findByProviderId("kakao-123")).thenReturn(Optional.of(existingUser));

        // Mock JWT token generation
        when(jwtTokenProvider.createToken(anyString(), isNull(), eq(TokenType.ACCESS)))
                .thenReturn("access-token");
        when(jwtTokenProvider.createToken(anyString(), isNull(), eq(TokenType.REFRESH)))
                .thenReturn("refresh-token");

        // When
        List<String> tokens = authService.authenticate(request, Provider.KAKAO);

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

        when(responseSpec.bodyToMono(KakaoUserDataResponse.class)).thenReturn(monoResponse);
        when(monoResponse.block()).thenReturn(null);

        // When/Then
        Exception exception = assertThrows(Exception.class, () -> {
            authService.authenticate(request, Provider.KAKAO);
        });
        assertEquals("카카오 서버에서 사용자 정보를 가져오는데 실패했습니다.", exception.getMessage());
    }
}
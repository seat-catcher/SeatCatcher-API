package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.dto.response.KakaoUserDataResponse;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.sullung2yo.seatcatcher.user.dto.request.TokenRequest;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProviderImpl jwtTokenProvider;

    /**
     * 지정된 인증 제공자와 토큰 요청 정보를 기반으로 사용자 인증 후 JWT 액세스 토큰과 리프레시 토큰을 생성합니다.
     *
     * <p>인증 방식에 따라 처리 로직이 달라집니다:
     * <ul>
     *   <li>LOCAL: 인증 처리를 수행하지 않고 null을 반환합니다.</li>
     *   <li>KAKAO: 카카오 API를 통해 사용자 정보를 조회한 후, 신규 사용자 등록 또는 기존 사용자 정보를 업데이트하고 JWT 토큰을 생성합니다.</li>
     *   <li>APPLE: 현재 구현되지 않아 null을 반환합니다.</li>
     * </ul>
     *
     * @param provider 사용자를 인증할 때 선택한 인증 제공자 (예: LOCAL, KAKAO, APPLE)
     * @param request  토큰 요청 정보가 담긴 객체로, 카카오 인증의 경우 provider access token을 포함합니다.
     * @return 카카오 인증 시 JWT 액세스 및 리프레시 토큰이 담긴 리스트, 그 외의 경우 null 반환
     * @throws Exception 제공자가 유효하지 않은 경우 예외 발생
     */
    public List<String> authenticate(Provider provider, TokenRequest request) throws Exception {
        WebClient webClient = WebClient.builder().build();

        // Request user information with provider_access_token in TokenRequest
        if (provider == Provider.LOCAL) {
            return null;
        }
        else if (provider == Provider.KAKAO) {
            // Get user information from Kakao
            User user = kakaoAuthenticator(webClient, request);

            // Generate JWT token (Access, Refresh)
            String accessToken = jwtTokenProvider.createToken(
                    user.getEmail(),
                    null,
                    TokenType.ACCESS
            );
            String refreshToken = jwtTokenProvider.createToken(
                    user.getEmail(),
                    null,
                    TokenType.REFRESH
            );
            return List.of(accessToken, refreshToken);
        }
        else if (provider == Provider.APPLE) {
            String appleDataUrl = "https://appleid.apple.com/auth/token";
            return null;
        }
        else {
            throw new Exception("Invalid provider");
        }
    }

    /**
     * 카카오 API를 호출하여 사용자 정보를 조회하고, 해당 정보를 기반으로 신규 사용자 등록 또는 기존 사용자 업데이트를 수행한 후 User 객체를 반환합니다.
     * 제공된 액세스 토큰을 사용하여 "<a href="https://kapi.kakao.com/v2/user/me">...</a>" 엔드포인트로 요청하며, 응답이 없으면 예외를 발생시킵니다.
     * 신규 사용자인 경우, 응답 정보를 토대로 User 객체를 생성하여 저장하며, 기존 사용자인 경우 이메일과 마지막 로그인 시간을 업데이트합니다.
     *
     * @param tokenRequest 카카오 API 호출에 필요한 액세스 토큰 정보를 담은 요청 객체
     * @throws Exception 카카오 API로부터 사용자 정보를 가져오지 못한 경우
     * @return 인증 또는 회원가입 처리된 사용자 정보를 담은 User 객체
     */
    private User kakaoAuthenticator(WebClient webClient, TokenRequest tokenRequest) throws Exception {
        String kakaoDataUrl = "https://kapi.kakao.com/v2/user/me";

        KakaoUserDataResponse response = webClient.get()
                .uri(kakaoDataUrl)
                .header("Authorization", "Bearer " + tokenRequest.getProviderAccessToken())
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoUserDataResponse.class)
                .block();

        if (response == null) {
            throw new Exception("Failed to get user information from Kakao");
        }

        // Check if the user is already registered
        String providerId = response.getId();
        User user = userRepository.findByProviderId(providerId).orElse(null);

        if (user == null) { // If new user
            user = User.builder()
                    .email(response.getKakaoAccount().getEmail())
                    .name("Random Name 123") // TODO: Implement random name generator !!!
                    .providerId(providerId)
                    .provider(Provider.KAKAO)
                    .role(UserRole.ROLE_USER)
                    .credit(0L)
                    .build();
            userRepository.save(user);
        }
        else { // If user already exists
            user.setEmail(response.getKakaoAccount().getEmail()); // update if email has changed
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return user;
    }

    /**
     * 애플 인증을 수행하는 메서드.
     * 현재 해당 메서드는 구현되어 있지 않으며, 향후 애플 인증 로직이 추가될 예정입니다.
     *
     * @param tokenRequest 애플 인증에 필요한 토큰 정보를 담은 객체
     * @return 인증된 사용자 정보. 현재는 구현되지 않아 null을 반환합니다.
     */
    private User appleAuthenticator(WebClient webClient, TokenRequest tokenRequest) {
        return null;
    }
}

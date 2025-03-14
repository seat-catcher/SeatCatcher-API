package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.dto.response.KakaoUserDataResponse;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.sullung2yo.seatcatcher.user.dto.request.TokenReqeust;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProviderImpl jwtTokenProvider;

    public List<String> authenticate(Provider provider, TokenReqeust request) throws Exception {
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

    private User kakaoAuthenticator(WebClient webClient, TokenReqeust tokenReqeust) throws Exception {
        String kakaoDataUrl = "https://kapi.kakao.com/v2/user/me";

        KakaoUserDataResponse response = webClient.get()
                .uri(kakaoDataUrl)
                .header("Authorization", "Bearer " + tokenReqeust.getProvider_access_token())
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
                    .email(response.getKakao_account().getEmail())
                    .name("Random Name 123") // TODO: Implement random name generator !!!
                    .providerId(providerId)
                    .provider(Provider.KAKAO)
                    .role(UserRole.ROLE_USER)
                    .credit(0L)
                    .build();
            userRepository.save(user);
        }
        else { // If user already exists
            user.setEmail(response.getKakao_account().getEmail()); // update if email has changed
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return user;
    }

    private User appleAuthenticator(WebClient webClient, TokenReqeust tokenReqeust) {
        return null;
    }
}

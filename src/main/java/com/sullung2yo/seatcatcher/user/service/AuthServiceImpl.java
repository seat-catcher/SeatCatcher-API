package com.sullung2yo.seatcatcher.user.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.request.AuthReqeust;
import com.sullung2yo.seatcatcher.user.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.response.KakaoUserDataResponse;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProviderImpl jwtTokenProvider;

    @Value("${apple.client.id}")
    private String appleClientId;

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
     * @param request  토큰 요청 정보가 담긴 객체로, 카카오 인증의 경우 provider access token을 포함합니다.
     * @return 카카오 인증 시 JWT 액세스 및 리프레시 토큰이 담긴 리스트, 그 외의 경우 null 반환
     * @throws Exception 제공자가 유효하지 않은 경우 예외 발생
     */
    public List<String> authenticate(AuthReqeust request) throws Exception {
        WebClient webClient = WebClient.builder().build();
        Provider provider = request.getProvider();

        // Request user information with provider_access_token in KakaoAuthRequest
        if (provider == Provider.LOCAL) {
            return null;
        }
        else if (provider == Provider.KAKAO) {
            // Get user information from Kakao
            KakaoAuthRequest kakaoAuthRequest = (KakaoAuthRequest) request; // Type cast to KakaoAuthRequest
            User user = kakaoAuthenticator(webClient, kakaoAuthRequest);

            // Generate JWT token (Access, Refresh)
            String accessToken = jwtTokenProvider.createToken(
                    user.getProviderId(),
                    null,
                    TokenType.ACCESS
            );
            String refreshToken = jwtTokenProvider.createToken(
                    user.getProviderId(),
                    null,
                    TokenType.REFRESH
            );
            return List.of(accessToken, refreshToken);
        }
        else if (provider == Provider.APPLE) {
            AppleAuthRequest appleAuthRequest = (AppleAuthRequest) request; // Type cast to AppleAuthRequest

            User user = appleAuthenticator(appleAuthRequest);

            // Generate JWT token (Access, Refresh)
            String accessToken = jwtTokenProvider.createToken(
                    user.getProviderId(),
                    null,
                    TokenType.ACCESS
            );
            String refreshToken = jwtTokenProvider.createToken(
                    user.getProviderId(),
                    null,
                    TokenType.REFRESH
            );
            return List.of(accessToken, refreshToken);
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
     * @param kakaoAuthRequest 카카오 API 호출에 필요한 액세스 토큰 정보를 담은 요청 객체
     * @throws Exception 카카오 API로부터 사용자 정보를 가져오지 못한 경우
     * @return 인증 또는 회원가입 처리된 사용자 정보를 담은 User 객체
     */
    private User kakaoAuthenticator(WebClient webClient, KakaoAuthRequest kakaoAuthRequest) throws Exception {
        String kakaoDataUrl = "https://kapi.kakao.com/v2/user/me";

        KakaoUserDataResponse response = webClient.get()
                .uri(kakaoDataUrl)
                .header("Authorization", "Bearer " + kakaoAuthRequest.getAccessToken())
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

        if (user == null) { // 만약 새로운 사용자라면
            user = User.builder()
                    .email("TEMP_EMAIL") // 토큰에 이메일 정보가 첨부되어 오는지 모르니까 일단 임시로 설정했습니다
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

    private User appleAuthenticator(AppleAuthRequest appleAuthRequest) throws Exception {
        String providerId = validateAppleIdentityToken(appleAuthRequest.getIdentityToken());
        User user = userRepository.findByProviderId(providerId).orElse(null);
        if (user == null) { // 만약 새로운 사용자라면
            user = User.builder()
                    .email("TEMP_EMAIL") // 토큰에 이메일 정보가 첨부되어 오는지 모르니까 일단 임시로 설정했습니다
                    .name("Random Name 123") // TODO: Implement random name generator !!!
                    .providerId(providerId)
                    .provider(Provider.APPLE)
                    .role(UserRole.ROLE_USER)
                    .credit(0L)
                    .build();
            userRepository.save(user);
        } else {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return user;
    }

    private String validateAppleIdentityToken(String identityToken) throws Exception {
        try {
            String issuer = "https://appleid.apple.com";
            JWT jwt = JWTParser.parse(identityToken);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet(); // Get JWT claims

            // 1. exp 검증
            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                throw new Exception("This identity token is expired");
            }

            // 2. iss 검증
            if (!claimsSet.getIssuer().equals(issuer)) {
                throw new Exception("This identity token is not issued by Apple");
            }

            // 3. aud 검증 ( Apple Developer 계정의 Client ID와 일치하는지 검증)
            if (!claimsSet.getAudience().contains(appleClientId)) {
                throw new Exception("This identity token is not intended for this client");
            }

            // 4. signature 검증
            SignedJWT signedJWT = (SignedJWT) jwt;
            String keyId = signedJWT.getHeader().getKeyID(); // 애플의 public Key 가져오기

            JWKSet jwkSet = loadApplePublicKeys();
            List<JWK> keys = jwkSet.getKeys();

            RSAKey publicKey = null;
            for (JWK key : keys) { // 애플의 Public Key와 일치하는 Key 찾기 -> 발견하면 성공
                if (key.getKeyID().equals(keyId)) {
                    publicKey = (RSAKey) key;
                    break;
                }
            }

            if (publicKey == null) {
                throw new Exception("Failed to find Apple public key");
            }

            RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new Exception("Failed to verify Apple identity token (Invalid signature)");
            }

            // 최종적으로 애플이 제공한 사용자 ID를 반환한다
            return claimsSet.getSubject();
        } catch (ParseException | JOSEException e) {
            throw new Exception("Failed to verify Apple identity token : ", e);
        }
    }

    private JWKSet loadApplePublicKeys() throws Exception {
        try {
            String applePublicKeyUrl = "https://appleid.apple.com/auth/keys";
            return JWKSet.load(new URL(applePublicKeyUrl));
        } catch (IOException e) {
            throw new Exception("Failed to load Apple public keys", e);
        }
    }
}

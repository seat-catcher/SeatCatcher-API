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
import com.sullung2yo.seatcatcher.user.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.user.dto.response.KakaoUserDataResponse;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProviderImpl jwtTokenProvider;
    private final WebClient webClient;

    @Value("${apple.client.id}")
    private String appleClientId;

    public AuthServiceImpl(UserRepository userRepository, JwtTokenProviderImpl jwtTokenProvider, WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.webClient = webClientBuilder.build();
    }

    public List<String> authenticate(Object request, Provider provider) throws Exception {

        if (provider == Provider.LOCAL) {
            return null;
        }
        else if (provider == Provider.KAKAO) {
            // 카카오에 사용자 정보 요청
            KakaoAuthRequest kakaoAuthRequest = (KakaoAuthRequest) request;
            User user = kakaoAuthenticator(kakaoAuthRequest);

            // JWT 토큰 생성 (Access, Refresh)
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
            log.debug("Apple authenticate 메서드 호출됨");

            // IdentityToken 검증 (이미 token에 필요한 정보가 들어있다)
            AppleAuthRequest appleAuthRequest = (AppleAuthRequest) request;
            User user = appleAuthenticator(appleAuthRequest);
            log.debug("Apple 토큰 검증 성공 및 사용자 갱신(생성) 완료 : {}", user);

            // JWT 토큰 생성 (Access, Refresh)
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
            log.debug("JWT 토큰 생성 완료: accessToken={}, refreshToken={}", accessToken, refreshToken);

            return List.of(accessToken, refreshToken);
        }
        else {
            throw new Exception("Invalid provider");
        }
    }

    private User kakaoAuthenticator(KakaoAuthRequest kakaoAuthRequest) throws Exception {
        String kakaoDataUrl = "https://kapi.kakao.com/v2/user/me";

        KakaoUserDataResponse response = webClient.get()
                .uri(kakaoDataUrl)
                .header("Authorization", "Bearer " + kakaoAuthRequest.getAccessToken())
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoUserDataResponse.class)
                .block();

        if (response == null) {
            throw new Exception("카카오 서버에서 사용자 정보를 가져오는데 실패했습니다.");
        }

        // Check if the user is already registered
        String providerId = response.getId();
        User user = userRepository.findByProviderId(providerId).orElse(null);

        if (user == null) { // If new user, register
            user = User.builder()
                    .name("Random Name 123") // TODO: Implement random name generator !!!
                    .providerId(providerId)
                    .provider(Provider.KAKAO)
                    .role(UserRole.ROLE_USER)
                    .credit(0L)
                    .build();
            userRepository.save(user);
        }
        else { // If user already exists, update last login time
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return user;
    }

    private User appleAuthenticator(AppleAuthRequest appleAuthRequest) throws Exception {
        log.debug("appleAuthenticator 메서드 호출됨");

        String providerId = validateAppleIdentityToken(appleAuthRequest.getIdentityToken());
        User user = userRepository.findByProviderId(providerId).orElse(null);
        if (user == null) { // if new user
            user = User.builder()
                    .name("Random Name 123") // TODO: Implement random name generator !!!
                    .providerId(providerId)
                    .provider(Provider.APPLE)
                    .role(UserRole.ROLE_USER)
                    .credit(0L)
                    .build();
            userRepository.save(user);
            log.debug("새로운 사용자 등록 완료: {}", user);
        } else {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.debug("기존 사용자 업데이트 완료: {}", user);
        }

        return user;
    }

    public String validateAppleIdentityToken(String identityToken) throws Exception {
        try {
            log.debug("validateAppleIdentityToken 메서드 호출됨 -> IdentityToken 검증 시작 : {}", identityToken);

            String issuer = "https://appleid.apple.com";
            String decodedToken = new String(Base64.getDecoder().decode(identityToken));
            log.debug("Base64 Decode : {}", decodedToken);

            JWT jwt = JWTParser.parse(decodedToken);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet(); // Get JWT claims
            log.debug("issuer: {}", issuer);
            log.debug("claimsSet: {}", claimsSet);

            // 1. exp 검증
            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                throw new Exception("This identity token is expired");
            }
            log.debug("성공적으로 exp 검증 완료");

            // 2. iss 검증
            if (!claimsSet.getIssuer().equals(issuer)) {
                throw new Exception("This identity token is not issued by Apple");
            }
            log.debug("성공적으로 iss 검증 완료");

            // 3. aud 검증 ( Apple Developer 계정의 Client ID와 일치하는지 검증)
            if (!claimsSet.getAudience().contains(appleClientId)) {
                throw new Exception("This identity token is not intended for this client");
            }
            log.debug("성공적으로 aud 검증 완료");

            // 4. signature 검증
            SignedJWT signedJWT = (SignedJWT) jwt;
            String keyId = signedJWT.getHeader().getKeyID(); // identity token의 Key ID 가져오기
            log.debug("keyId: {}", keyId);

            JWKSet jwkSet = loadApplePublicKeys();
            List<JWK> keys = jwkSet.getKeys(); // Apple에서 제공하는 Public Key 가져오기
            log.debug("apple provide keys: {}", keys);

            RSAKey publicKey = null;
            for (JWK key : keys) { // 애플의 Public Key와 일치하는 Key 찾기 -> 발견하면 성공
                if (key.getKeyID().equals(keyId)) {
                    try {
                        publicKey = (RSAKey) key;
                        log.debug("매칭되는 key를 찾았어요: {}", publicKey);
                        break;
                    } catch (Exception e) {
                        log.error("키 매칭 중 에러 발생: {}", e.getMessage());
                        throw new Exception("키 매칭 중 에러 발생 : " + e.getMessage());
                    }
                }
            }

            if (publicKey == null) {
                throw new Exception("identityToken에서 추출한 keyId와 애플에서 제공하는 Public KeyId가 일치하지 않습니다.");
            }

            RSASSAVerifier verifier = new RSASSAVerifier(publicKey.toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                log.error("identityToken의 signature 검증 실패");
                throw new Exception("identityToken의 signature 검증 실패");
            }

            // 최종적으로 애플이 제공한 사용자 ID를 반환한다
            log.debug("Apple identityToken 검증 성공! 사용자 ID 반환: {}", claimsSet.getSubject());
            return claimsSet.getSubject();

        } catch (ParseException e) {
            log.error("Parse error during token validation", e);
            throw new Exception("애플 IdentityToken 검증 오류: " + e.getMessage());
        } catch (JOSEException e) {
            log.error("JOSE error during token validation", e);
            throw new Exception("애플 IdentityToken 검증 오류: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            throw new Exception("애플 IdentityToken 검증 오류: " + e.getMessage());
        }
    }

    private JWKSet loadApplePublicKeys() throws Exception {
        try {
            String applePublicKeyUrl = "https://appleid.apple.com/auth/keys";
            return JWKSet.load(new URL(applePublicKeyUrl));
        } catch (IOException e) {
            throw new Exception("애플 PublicKey를 가져오는데 실패했습니다", e);
        }
    }
}

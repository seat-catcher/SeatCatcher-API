package com.sullung2yo.seatcatcher.domain.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sullung2yo.seatcatcher.common.exception.AuthException;
import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.enums.UserRole;
import com.sullung2yo.seatcatcher.domain.auth.dto.request.AppleAuthRequest;
import com.sullung2yo.seatcatcher.domain.auth.dto.request.KakaoAuthRequest;
import com.sullung2yo.seatcatcher.domain.auth.dto.response.KakaoUserDataResponse;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.user.utility.random.NameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProviderImpl jwtTokenProvider;
    private final WebClient webClient;
    private final NameGenerator nameGenerator;
    private final ResourceLoader resourceLoader;

    @Value("${apple.client.id}")
    private String appleClientId;

    @Value("${apple.team.id}")
    private String appleTeamId;

    @Value("${apple.key.id}")
    private String appleKeyId;

    @Value("${apple.private_key_pem}")
    private String applePrivateKeyPath; // 로컬, 로컬 테스트 환경에서만 사용

    @Value("${apple.private_key_content}")
    private String applePrivateKeyContent; // CICD, PROD 환경에서 사용

    public AuthServiceImpl(
            UserRepository userRepository,
            JwtTokenProviderImpl jwtTokenProvider,
            WebClient.Builder webClientBuilder,
            NameGenerator nameGenerator,
            ResourceLoader resourceLoader
    ) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.webClient = webClientBuilder.build();
        this.nameGenerator = nameGenerator;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public List<String> authenticate(Object request, Provider provider) {

        if (provider == Provider.KAKAO) {
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
            log.info("JWT 토큰 생성 완료: accessToken={}, refreshToken={}", accessToken, refreshToken);
            return List.of(accessToken, refreshToken);
        } else {
            throw new AuthException("지원하지 않는 Provider 입니다.", ErrorCode.AUTH_INVALID_PROVIDER);
        }
    }

    @Override
    public List<String> refreshToken(String token) {
        return jwtTokenProvider.refreshToken(token);
    }

    @Override
    public Boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token, TokenType.ACCESS);
    }

    private User kakaoAuthenticator(KakaoAuthRequest kakaoAuthRequest) {
        String kakaoDataUrl = "https://kapi.kakao.com/v2/user/me";

        KakaoUserDataResponse response = webClient.get()
                .uri(kakaoDataUrl)
                .header("Authorization", "Bearer " + kakaoAuthRequest.getAccessToken())
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoUserDataResponse.class)
                .block();

        if (response == null) {
            throw new AuthException("카카오 서버에서 사용자 정보를 가져오는데 실패했습니다.", ErrorCode.AUTH_KAKAO_SERVER_ERROR);
        }

        // Check if the user is already registered
        String providerId = response.getId();
        User user = userRepository.findByProviderId(providerId).orElse(null);

        if (user == null) { // If new user, register
            user = User.builder()
                    .name(nameGenerator.generateRandomName())
                    .providerId(providerId)
                    .fcmToken(kakaoAuthRequest.getFcmToken())
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

    private User appleAuthenticator(AppleAuthRequest appleAuthRequest) {
        log.debug("appleAuthenticator 메서드 호출됨");

        String providerId = validateAppleIdentityToken(appleAuthRequest.getIdentityToken(), appleAuthRequest.getNonce());
        User user = userRepository.findByProviderId(providerId).orElse(null);
        if (user == null) { // if new user
            user = User.builder()
                    .name(nameGenerator.generateRandomName())
                    .providerId(providerId)
                    .fcmToken(appleAuthRequest.getFcmToken())
                    .appleAuthorizationCode(appleAuthRequest.getAuthorizationCode())
                    .provider(Provider.APPLE)
                    .role(UserRole.ROLE_USER)
                    .credit(0L)
                    .build();
            userRepository.save(user);
            log.debug("새로운 사용자 등록 완료: {}", user);
        } else {
            user.setLastLoginAt(LocalDateTime.now());
            user.setAppleAuthorizationCode(appleAuthRequest.getAuthorizationCode()); // 새로운 auth code로 업데이트
            userRepository.save(user);
            log.debug("기존 사용자 업데이트 완료: {}", user);
        }

        return user;
    }

    @Override
    public String validateAppleIdentityToken(String identityToken, String expectedNonce) {
        try {
            log.debug("Apple Identity Token 검증 시작");
            
            // 1. JWT 파싱 (Base64 디코딩 제거 - identityToken은 이미 JWT 형태)
            SignedJWT signedJWT = SignedJWT.parse(identityToken);
            String keyId = signedJWT.getHeader().getKeyID();
            log.debug("Token에서 추출한 Key ID: {}", keyId);

            // 2. Apple 공개키 로드
            JWKSet jwkSet = loadApplePublicKeys();
            RSAKey publicKey = null;
            
            for (JWK key : jwkSet.getKeys()) {
                if (keyId.equals(key.getKeyID()) && key instanceof RSAKey) {
                    publicKey = (RSAKey) key;
                    break;
                }
            }

            if (publicKey == null) {
                throw new TokenException("Apple 공개키에서 매칭되는 Key ID를 찾을 수 없습니다: " + keyId, ErrorCode.INVALID_TOKEN);
            }

            // 3. 서명 검증
            RSASSAVerifier verifier = new RSASSAVerifier(publicKey.toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                throw new TokenException("Identity Token 서명 검증 실패", ErrorCode.INVALID_TOKEN);
            }
            log.debug("서명 검증 성공");

            // 4. Claims 추출 및 검증
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            
            // 토큰 만료시간 검증 (Apple 토큰 유효기간 10min)
            Date expirationTime = claimsSet.getExpirationTime();
            Date currentTime = new Date();
            if (expirationTime == null || expirationTime.before(currentTime)) {
                throw new TokenException("Identity Token이 만료되었습니다", ErrorCode.EXPIRED_TOKEN);
            }
            log.debug("토큰 만료시간 검증 성공");

            // iss 검증
            String issuer = claimsSet.getIssuer();
            if (!"https://appleid.apple.com".equals(issuer)) {
                throw new TokenException("잘못된 토큰 발급자: " + issuer, ErrorCode.INVALID_TOKEN);
            }
            log.debug("발급자 검증 성공");

            // aud 검증
            List<String> audience = claimsSet.getAudience();
            log.debug("토큰의 Audience: {}, 설정된 Client ID: {}", audience, appleClientId);
            if (audience == null || !audience.contains(appleClientId)) {
                throw new TokenException("토큰 대상이 일치하지 않습니다", ErrorCode.INVALID_TOKEN);
            }
            log.debug("대상(Audience) 검증 성공");

            // nonce 검증
            if (expectedNonce != null && !expectedNonce.isEmpty()) {
                String tokenNonce = (String) claimsSet.getClaim("nonce");
                if (!expectedNonce.equals(tokenNonce)) {
                    throw new TokenException("Nonce 값이 일치하지 않습니다", ErrorCode.INVALID_TOKEN);
                }
                log.debug("Nonce 검증 성공");
            }

            // 5. 사용자 ID 반환
            String subject = claimsSet.getSubject();
            if (subject == null || subject.isEmpty()) {
                throw new TokenException("토큰에서 사용자 ID를 찾을 수 없습니다", ErrorCode.INVALID_TOKEN);
            }
            
            log.info("Apple Identity Token 검증 완료. 사용자 ID: {}", subject);
            return subject;

        } catch (ParseException e) {
            log.error("토큰 파싱 오류: {}", e.getMessage());
            throw new TokenException("Identity Token 파싱 실패: " + e.getMessage(), ErrorCode.INVALID_TOKEN);
        } catch (JOSEException e) {
            log.error("JWT 검증 오류: {}", e.getMessage());
            throw new TokenException("Identity Token 검증 실패: " + e.getMessage(), ErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage());
            throw new TokenException("Identity Token 검증 중 오류 발생: " + e.getMessage(), ErrorCode.INVALID_TOKEN);
        }
    }

    private JWKSet loadApplePublicKeys() {
        try {
            String applePublicKeyUrl = "https://appleid.apple.com/auth/keys";
            return JWKSet.load(new URL(applePublicKeyUrl));
        } catch (ParseException | IOException e) {
            throw new AuthException("애플 PublicKey를 파싱하는데 실패했습니다", ErrorCode.AUTH_PARSE_ERROR);
        }
    }

    /**
     * Apple Sign in with Apple 토큰 취소 메서드
     */
    @Override
    public void revokeAppleToken(String authorizationCode) {
        try {
            log.debug("Apple 토큰 취소 시작. 사용자 auth code: {}", authorizationCode);
            
            // 1. Client Secret JWT 생성
            String clientSecret = generateAppleClientSecret();
            
            // 2. Apple 토큰 취소 API 호출
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("client_id", appleClientId);
            requestBody.put("client_secret", clientSecret);
            requestBody.put("token", authorizationCode); // auth code 를 토큰으로 사용
            requestBody.put("token_type_hint", "authorization_code"); // 토큰 타입

            String response = webClient.post()
                    .uri("https://appleid.apple.com/auth/revoke")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(buildFormUrlEncoded(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Apple 토큰 취소 완료. 응답: {}", response);
            
        } catch (Exception e) {
            log.error("Apple 토큰 취소 실패. 삭제는 계속 진행합니다. : {}", e.getMessage());
            // 토큰 취소 실패 시에도 사용자 삭제는 계속 진행
            // Apple 가이드라인에 따라 사용자 요청을 거부하면 안됨
        }
    }

    /**
     * Apple Client Secret JWT 생성
     * Apple 토큰 취소 API 호출 시 필요한 Client secret을 jwt 형태로 생성해준다.
     */
    private String generateAppleClientSecret() {
        try {
            // 1. JWT Header 생성
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(appleKeyId)
                    .build();

            // 2. JWT Claims 생성
            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .issuer(appleTeamId)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(15777000))) // 6개월
                    .audience("https://appleid.apple.com") // 대상
                    .subject(appleClientId)
                    .build();

            // 3. 개인키 로드 (실제 구현에서는 파일이나 환경변수에서 로드)
            ECPrivateKey privateKey = loadApplePrivateKey();

            // 4. JWT 서명 및 생성
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(new ECDSASigner(privateKey));

            return signedJWT.serialize();

        } catch (Exception e) {
            log.error("Apple Client Secret JWT 생성 실패: {}", e.getMessage());
            throw new AuthException("Apple Client Secret 생성 실패", ErrorCode.AUTH_APPLE_CLIENT_SECRET_ERROR);
        }
    }

    /**
     * Apple 개인키 로드 (환경변수 우선, 파일 fallback)
     */
    private ECPrivateKey loadApplePrivateKey() {
        try {
            log.debug("Apple private key 로드 시작");
            
            String privateKeyContent;
            
            // 1. 환경변수에서 키 내용 확인 (CI/CD 환경, Prod 환경용)
            if (applePrivateKeyContent != null && !applePrivateKeyContent.trim().isEmpty()) {
                log.debug("환경변수에서 Apple private key 로드");
                privateKeyContent = applePrivateKeyContent;
            }
            // 2. 파일에서 키 로드 (로컬 개발, 로컬 Test 환경용)
            else if (applePrivateKeyPath != null && !applePrivateKeyPath.trim().isEmpty()) {
                log.debug("파일에서 Apple private key 로드: {}", applePrivateKeyPath);
                Resource resource = resourceLoader.getResource(applePrivateKeyPath);
                if (!resource.exists()) {
                    throw new AuthException("Apple Private key 파일을 찾을 수 없습니다: " + applePrivateKeyPath, ErrorCode.AUTH_APPLE_PRIVATE_KEY_ERROR);
                }
                privateKeyContent = new String(resource.getInputStream().readAllBytes());
            }
            // 3. 둘 다 없으면 오류
            else {
                throw new AuthException("Apple private key가 설정되지 않았습니다. 환경변수 APPLE_PRIVATE_KEY_CONTENT 또는 apple.private_key_pem 설정이 필요합니다.", ErrorCode.AUTH_APPLE_PRIVATE_KEY_ERROR);
            }
            
            // 4. PEM 형식 정리 및 Base64 디코딩
            String privateKeyPEM = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", ""); // 공백, 줄바꿈 제거
            
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            
            // 5. PKCS8 형태로 EC 개인키 생성
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);
            
            log.info("Apple Private Key 로드 성공");
            return privateKey;
            
        } catch (Exception e) {
            log.error("Apple private key 로드 실패: {}", e.getMessage());
            throw new AuthException("Apple private key 로드 실패: " + e.getMessage(), ErrorCode.AUTH_APPLE_PRIVATE_KEY_ERROR);
        }
    }

    /**
     * Form URL Encoded 형태로 변환
     */
    private String buildFormUrlEncoded(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");
    }
}

package com.sullung2yo.seatcatcher.common.utility;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.TokenProvider;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user.domain.UserRole;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Profile("dev") // Only for Local development and Dev environment
@RequiredArgsConstructor
public class DevAdminTokenGenerator {

    @Value("${jwt.admin.subject}")
    private String adminSubject;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @PostConstruct
    public void generateAdminToken() {
        // Admin이 있는지 확인
        Optional<User> adminUser = userRepository.findByProviderId(adminSubject);
        User user;
        if (adminUser.isPresent()) {
            user = adminUser.get();
        }
        else {
            user = User.builder()
                    .name("Admin")
                    .providerId(adminSubject)
                    .provider(Provider.LOCAL)
                    .role(UserRole.ROLE_ADMIN)
                    .deviceStatus(true)
                    .build();
            userRepository.save(user);
        }

        // 토큰 생성
        Map<String, ?> payload = Map.of(
                "role", UserRole.ROLE_ADMIN
        );
        String adminAccessToken = tokenProvider.createToken(
                adminSubject,
                payload,
                TokenType.ACCESS
        );
        String adminRefreshToken = tokenProvider.createToken(
                adminSubject,
                payload,
                TokenType.REFRESH
        );

        log.debug("\u001B[32mAccess token for development : {}\u001B[0m", adminAccessToken);
        log.debug("\u001B[34mRefresh token for development : {}\u001B[0m", adminRefreshToken);
    }
}

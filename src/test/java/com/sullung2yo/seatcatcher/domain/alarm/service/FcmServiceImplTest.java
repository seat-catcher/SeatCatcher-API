package com.sullung2yo.seatcatcher.domain.alarm.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.domain.alarm.dto.request.FcmRequest;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.enums.UserRole;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FcmServiceImplTest {

    @Autowired
    private FcmService fcmService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("saveToken 정상 동작 - 실제 유저를 등록하고 FCM 토큰 저장")
    void saveToken_정상동작() {
        // given
        User user = new User();
        user.setProviderId("test_provider");
        user.setName("테스트 유저");
        user.setEmail("test@example.com");
        user.setPassword("dummy");
        user.setRole(UserRole.ROLE_USER);
        user.setProvider(Provider.KAKAO);
        user.setFcmToken(null);
        userRepository.save(user);

        setAuthentication("test_provider");

        FcmRequest.Token tokenRequest = new FcmRequest.Token("test_token");

        // when
        fcmService.saveToken(tokenRequest);

        // then
        User updatedUser = userRepository.findByProviderId("test_provider").orElseThrow();
        assertEquals("test_token", updatedUser.getFcmToken());
    }

    @Test
    @DisplayName("saveToken 실패 - 유저 없음으로 인한 예외 발생")
    void saveToken_userNotFound() {
        // given
        setAuthentication("missing_provider");

        FcmRequest.Token tokenRequest = new FcmRequest.Token("test_token");

        // when + then
        UserException ex = assertThrows(UserException.class, () -> {
            fcmService.saveToken(tokenRequest);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("해당 id를 가진 사용자를 찾을 수 없습니다"));
    }

    private void setAuthentication(String providerId) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(providerId, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
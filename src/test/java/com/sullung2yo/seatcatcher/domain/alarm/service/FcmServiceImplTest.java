package com.sullung2yo.seatcatcher.domain.alarm.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.domain.alarm.dto.request.FcmRequest;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmServiceImplTest {

    @InjectMocks
    private FcmServiceImpl fcmService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("saveToken 정상 동작 - 인증된 사용자의 FCM 토큰 저장")
    void saveToken_정상동작() {
        // given
        FcmRequest fcmRequest = new FcmRequest();
        FcmRequest.Token tokenRequest = fcmRequest.new Token("test_token");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test_provider");

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(authentication);

        User user = new User();
        user.setFcmToken(null); // 초기 상태
        when(userRepository.findByProviderId("test_provider")).thenReturn(Optional.of(user));

        // when + then
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            fcmService.saveToken(tokenRequest);

            assertEquals("test_token", user.getFcmToken());
        }
    }

    @Test
    @DisplayName("saveToken 실패 - 유저를 찾을 수 없음")
    void saveToken_userNotFound() {
        // given
        FcmRequest fcmRequest = new FcmRequest();
        FcmRequest.Token tokenRequest = fcmRequest.new Token("test_token");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("missing_provider");

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.getAuthentication()).thenReturn(authentication);

        when(userRepository.findByProviderId("missing_provider"))
                .thenReturn(Optional.empty());

        // when + then
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            UserException thrown = assertThrows(UserException.class, () -> {
                fcmService.saveToken(tokenRequest);
            });

            assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());
            assertTrue(thrown.getMessage().contains("해당 id를 가진 사용자를 찾을 수 없습니다"));
        }
    }
}
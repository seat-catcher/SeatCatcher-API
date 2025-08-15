package com.sullung2yo.seatcatcher.domain.alarm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.domain.alarm.dto.request.FcmRequest;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.enums.UserRole;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FcmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test_provider")
    @DisplayName("FCM 토큰 저장 성공 - 유효한 사용자일 경우")
    void saveFcmToken_success() throws Exception {
        // given: 유저 생성 및 저장
        User user = new User();
        user.setProviderId("test_provider");
        user.setFcmToken(null);
        user.setName("Test User");
        user.setPassword("test1234");
        user.setEmail("test@example.com");
        user.setRole(UserRole.ROLE_USER);
        user.setProvider(Provider.KAKAO);
        userRepository.save(user);

        // 인증 정보 설정
        setAuthentication("test_provider");

        // 요청 객체 생성
        FcmRequest.Token request = new FcmRequest.Token("test_token");

        // when + then: 요청 수행 및 검증
        mockMvc.perform(post("/fcm/token")
                        .with(authentication(new UsernamePasswordAuthenticationToken("test_provider", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // DB에서 유저를 다시 불러와 토큰이 저장됐는지 확인
        User updatedUser = userRepository.findByProviderId("test_provider").orElseThrow();
        assertEquals("test_token", updatedUser.getFcmToken());
    }

    @Test
    @WithMockUser(username = "test_provider")
    @DisplayName("FCM 토큰 저장 실패 - 사용자 없음")
    void saveFcmToken_userNotFound() throws Exception {
        // 인증 정보 설정 (없는 사용자 ID)
        setAuthentication("not_exist_user");

        FcmRequest.Token request = new FcmRequest.Token("test_token");

        mockMvc.perform(post("/fcm/token")
                        .with(authentication(new UsernamePasswordAuthenticationToken("test_provider", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // 여기 고침
    }

    private void setAuthentication(String providerId) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(providerId, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
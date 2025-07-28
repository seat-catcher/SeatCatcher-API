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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
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
    @DisplayName("FCM 토큰 저장 성공 - 유효한 사용자일 경우")
    void saveFcmToken_success() throws Exception {
        // given
        User user = new User();
        user.setProviderId("test_provider");
        user.setFcmToken(null);
        user.setName("Test User"); // 필수
        user.setPassword("test1234"); // 만약 null 불가면
        user.setEmail("test@example.com"); // 마찬가지
        user.setRole(UserRole.ROLE_USER); // enum 값 채워야 함
        user.setProvider(Provider.KAKAO); // Enum 값도 정확히 존재하는 값으로
        userRepository.save(user);

        setAuthentication("test_provider");

        FcmRequest.Token request = new FcmRequest().new Token("test_token");

        // when + then
        mockMvc.perform(post("/fcm/token")
                        .with(authentication(new UsernamePasswordAuthenticationToken("test_provider", null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findByProviderId("test_provider").orElseThrow();
        assertEquals("test_token", updatedUser.getFcmToken());
    }

    @Test
    @DisplayName("FCM 토큰 저장 실패 - 사용자 없음")
    void saveFcmToken_userNotFound() throws Exception {
        // given
        setAuthentication("missing_provider");

        FcmRequest.Token request = new FcmRequest().new Token("test_token");

        // when + then
        mockMvc.perform(post("/fcm/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    private void setAuthentication(String providerId) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(providerId, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}

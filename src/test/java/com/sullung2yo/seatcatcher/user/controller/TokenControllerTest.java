package com.sullung2yo.seatcatcher.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;

import com.sullung2yo.seatcatcher.user.dto.request.TokenRefreshRequest;
import com.sullung2yo.seatcatcher.user.repository.RefreshTokenRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class TokenControllerTest {

    @Autowired
    MockMvc mockMvc; // 요청 보내주는 MockMvc 객체

    @Autowired
    ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper 객체

    @Autowired
    private JwtTokenProviderImpl jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenValidMilliseconds", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidMilliseconds", 3600000L);
        testUser = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .name("testUser")
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("토큰 갱신 테스트")
    void testRefreshToken() throws Exception {
        //Given
        String refreshToken = jwtTokenProvider.createToken(testUser.getProviderId(), null, TokenType.REFRESH);
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken(refreshToken);

        //When
        String requestBody = objectMapper.writeValueAsString(tokenRefreshRequest);

        //Then
        mockMvc.perform(post("/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON) // ContentType 설정
                        .content(requestBody) // RequestBody 설정
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 갱신 요청하는 경우 테스트")
    void testExpiredRefreshToken() throws Exception {
        // Given
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenValidMilliseconds", 0L);
        String expired_refreshToken = jwtTokenProvider.createToken(testUser.getProviderId(), null, TokenType.REFRESH);

        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken(expired_refreshToken);

        // When
        String requestBody = objectMapper.writeValueAsString(tokenRefreshRequest);

        // Then
        mockMvc.perform(post("/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @DisplayName("Access Token 유효성 검사 테스트")
    void testValidateAccessToken() throws Exception {
        // Given
        String accessToken = jwtTokenProvider.createToken(testUser.getProviderId(), null, TokenType.ACCESS);

        // When
        mockMvc.perform(get("/token/validate")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Valid"));
    }

    @Test
    @DisplayName("만료된 Access Token으로 유효성 검사 요청하는 경우 테스트")
    void testExpiredAccessToken() throws Exception {
        // Given
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidMilliseconds", 0L);
        String expiredAccessToken = jwtTokenProvider.createToken(testUser.getProviderId(), null, TokenType.ACCESS);

        // When
        mockMvc.perform(get("/token/validate")
                        .header("Authorization", "Bearer " + expiredAccessToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Expired"));
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }
}
package com.sullung2yo.seatcatcher.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.filter.JwtAuthenticationFilter;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.dto.request.TokenRefreshRequest;
import com.sullung2yo.seatcatcher.user.repository.RefreshTokenRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = TokenController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class TokenControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtTokenProviderImpl tokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthServiceImpl authService;

    private User user;
    private final String mockAccessToken = "mock-access-token";
    private final String mockRefreshToken = "mock-refresh-token";

    @BeforeEach
    void setUp() throws Exception {
        // 사용자 생성
        user = User.builder()
                .provider(Provider.APPLE)
                .providerId("test")
                .role(UserRole.ROLE_USER)
                .name("test")
                .build();
        when(authService.refreshToken(anyString())).thenReturn(List.of(mockAccessToken, mockRefreshToken));
    }

    @Test
    void tokenRefresh() throws Exception {
        // Given
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken(mockRefreshToken);

        // When & Then
        mvc.perform(post("/token/refresh")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value(mockAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(mockRefreshToken));
    }
}
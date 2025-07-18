package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 데이터베이스 롤백
class TagControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProviderImpl jwtTokenProvider;

    private static User user;
    private static String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트할 사용자 생성
        user = User.builder()
                .provider(Provider.APPLE)
                .providerId(String.valueOf(System.currentTimeMillis()))
                .name("testUser")
                .credit(123L)
                .profileImageNum(ProfileImageNum.IMAGE_1)
                .build();
        userRepository.save(user);

        // Access 토큰 생성
        accessToken = jwtTokenProvider.createToken(user.getProviderId(), null, TokenType.ACCESS);
    }

    @Test
    void getTags() throws Exception {
        mockMvc.perform(get("/tags")
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json"))
                .andExpect(status().isOk())
                // 최소한 1개 이상의 태그가 있는지 확인
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists())
                // 각 태그가 필요한 필드를 포함하는지 확인
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].tagName").exists());
    }

    @Test
    void getTagById() throws Exception {
        // Given
        // ID가 1인 태그가 존재하고 이름이 USERTAG_NULL이라고 가정

        // When, Then
        mockMvc.perform(get("/tags/{id}", 1L)
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tagName").value(UserTagType.USERTAG_NULL.name()));
    }

    @Test
    void getTagById_NotFound() throws Exception {
        // given: 존재하지 않는 태그 ID
        Long nonExistentId = 9999L;

        // when & then
        mockMvc.perform(get("/tags/{id}", nonExistentId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }
}
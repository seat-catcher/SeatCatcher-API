package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.repository.UserTagRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private JwtTokenProviderImpl jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserTagRepository userTagRepository;

    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트할 사용자 생성
        Tag tag = Tag.builder()
                .tagName(UserTagType.USERTAG_NULL)
                .build();
        tagRepository.save(tag);

        User user = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .name("testUser")
                .credit(123L)
                .profileImageNum(ProfileImageNum.IMAGE_1)
                .build();
        userRepository.save(user);

        UserTag userTag = UserTag.builder()
                .user(user)
                .tag(tag)
                .build();
        userTag.setRelationships(user, tag);
        userTagRepository.save(userTag);

        // Access 토큰 생성
        accessToken = jwtTokenProvider.createToken(user.getProviderId(), null, TokenType.ACCESS);
    }

    @Test
    void getUserInformationWithoutToken() throws Exception {
        // When
        mockMvc.perform(get("/user/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserInformation() throws Exception {
        // Then
        mockMvc.perform(get("/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.profileImageNum").exists())
                .andExpect(jsonPath("$.credit").exists())
                .andExpect(jsonPath("$.tags").exists());
    }

    @AfterEach
    void tearDown() {
        userTagRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();
    }
}
package com.sullung2yo.seatcatcher.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.user.dto.request.UserInformationUpdateRequest;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.repository.UserTagRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
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

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;

    private User user;

    @BeforeEach
    void setUp() {
        // 테스트할 사용자 생성
        user = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .name("testUser")
                .credit(123L)
                .profileImageNum(ProfileImageNum.IMAGE_1)
                .build();
        userRepository.save(user);

        Tag tag = tagRepository.findByTagName(UserTagType.USERTAG_CARRIER).get();

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

    @Test
    void updateUserInformation() throws Exception{
        // Given
        UserInformationUpdateRequest userInformationUpdateRequest = UserInformationUpdateRequest.builder()
                .credit(555L)
                .tags(List.of(UserTagType.USERTAG_NULL, UserTagType.USERTAG_LONGDISTANCE))
                .build();

        // When
        mockMvc.perform(patch("/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInformationUpdateRequest))
                .header("Authorization", "Bearer " + accessToken))

        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.profileImageNum").exists())
                .andExpect(jsonPath("$.credit").value(555L))
                .andExpect(jsonPath("$.tags").exists());
    }

    @Test
    void deleteUser() throws Exception {
        // Given
        // User given in setUp()

        // When
        mockMvc.perform(delete("/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken))

        // Then
                .andExpect(status().isNoContent());

        // When
        List<UserTag> userTags = userTagRepository.findUserTagByUser(user);
        Optional<User> foundUser = userRepository.findByProviderId("testProviderId");
        assertThat(foundUser).isEmpty();
        assertThat(userTags).isEmpty();
    }

    @AfterEach
    void tearDown() {
        userTagRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();
    }

}
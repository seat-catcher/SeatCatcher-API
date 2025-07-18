package com.sullung2yo.seatcatcher.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.tag.entity.Tag;
import com.sullung2yo.seatcatcher.domain.tag.entity.UserTag;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.user.dto.request.UserInformationUpdateRequest;
import com.sullung2yo.seatcatcher.domain.tag.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.tag.repository.UserTagRepository;
import org.springframework.transaction.annotation.Transactional;
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

        Optional<Tag> optionalTag = tagRepository.findByTagName(UserTagType.USERTAG_CARRIER);
        if (optionalTag.isEmpty()) {
            throw new RuntimeException("Tag not found");
        }
        Tag tag = optionalTag.get();

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
                .profileImageNum(ProfileImageNum.IMAGE_2) // IMAGE_1 -> IMAGE_2
                .tags(List.of(UserTagType.USERTAG_NULL, UserTagType.USERTAG_LONGDISTANCE)) // USERTAG_CARRIER -> USERTAG_NULL, USERTAG_LONGDISTANCE
                .hasOnBoarded(true) // 최초 사용자 생성 시 false이므로 true로 변경 시도
                .build();

        // When
        mockMvc.perform(patch("/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userInformationUpdateRequest))
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Then
        mockMvc.perform(get("/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageNum").value(userInformationUpdateRequest.getProfileImageNum().name()))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.hasOnBoarded").value(userInformationUpdateRequest.getHasOnBoarded()));
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

    @Test
    void deleteAppleUser() throws Exception {
        // Given - Apple 사용자 생성
        User appleUser = User.builder()
                .name("Apple Test User")
                .providerId("appleProviderId")
                .fcmToken("appleFcmToken")
                .provider(Provider.APPLE)
                .role(UserRole.ROLE_USER)
                .credit(0L)
                .build();
        userRepository.save(appleUser);

        String appleAccessToken = jwtTokenProvider.createToken(
                appleUser.getProviderId(),
                null,
                TokenType.ACCESS
        );

        // When - Apple 사용자 삭제 요청
        mockMvc.perform(delete("/user/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + appleAccessToken))

        // Then - 정상적으로 삭제됨 (토큰 취소 실패해도 삭제 진행)
                .andExpect(status().isNoContent());

        // Apple 사용자가 데이터베이스에서 삭제되었는지 확인
        Optional<User> foundAppleUser = userRepository.findByProviderId("appleProviderId");
        assertThat(foundAppleUser).isEmpty();
    }

    @AfterEach
    void tearDown() {
        userTagRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();
    }

}
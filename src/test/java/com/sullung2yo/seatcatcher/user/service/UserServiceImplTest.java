package com.sullung2yo.seatcatcher.user.service;

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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest
@Transactional
public class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserTagRepository userTagRepository;

    @Autowired
    private JwtTokenProviderImpl jwtTokenProvider;

    private String accessToken;

    private User user;
    @Autowired
    private UserService userService;

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
    void testGetUserWithToken() {
        // When
        User retrievedUser = userService.getUserWithToken(accessToken);

        // Then
        assertThat(retrievedUser.getProviderId()).isEqualTo(user.getProviderId());
        assertThat(retrievedUser.getName()).isEqualTo(user.getName());
        assertThat(retrievedUser.getCredit()).isEqualTo(user.getCredit());
        assertThat(retrievedUser.getProfileImageNum()).isEqualTo(user.getProfileImageNum());
    }

    @Test
    void testUpdateUser() {
        // Given
        UserInformationUpdateRequest userInformationUpdateRequest = UserInformationUpdateRequest.builder()
                .name("updatedName")
                .profileImageNum(ProfileImageNum.IMAGE_2)
                .hasOnBoarded(true)
                .tags(List.of(UserTagType.USERTAG_CARRIER))
                .appleAuthorizationCode("sampleAuthorizationCode")
                .build();

        // When
        User updatedUser = userService.updateUser(accessToken, userInformationUpdateRequest);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("updatedName");
        assertThat(updatedUser.getProfileImageNum()).isEqualTo(ProfileImageNum.IMAGE_2);
        assertThat(updatedUser.getHasOnBoarded()).isTrue();
        assertThat(updatedUser.getAppleAuthorizationCode()).isEqualTo("sampleAuthorizationCode");
    }
}

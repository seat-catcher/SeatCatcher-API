package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.repository.UserTagRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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

//    @Test
//    void increaseCreditForInternalImplementTest()
//    {
//        // When
//        userService.creditModification(user.getId(), 100L, true);
//
//        // then
//        assertThat(user.getCredit()).isEqualTo(223L);
//    }
//
//    @Test
//    void decreaseCreditForInternalImplementTest()
//    {
//        // When
//        userService.creditModification(user.getId(), 100L, false);
//
//        // Then
//        assertThat(user.getCredit()).isEqualTo(23L);
//
//        // When & Then
//        assertThatThrownBy(() -> userService.creditModification(user.getId(), 10000L, false))
//                .isInstanceOf(UserException.class)
//                .hasMessageContaining(ErrorCode.INSUFFICIENT_CREDIT.getMessage());
//    }

}

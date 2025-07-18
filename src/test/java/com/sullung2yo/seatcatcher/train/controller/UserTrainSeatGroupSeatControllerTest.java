package com.sullung2yo.seatcatcher.train.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.tag.entity.Tag;
import com.sullung2yo.seatcatcher.domain.tag.entity.UserTag;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.domain.tag.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.tag.repository.UserTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
@Rollback
public class UserTrainSeatGroupSeatControllerTest {

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
    private TrainSeatGroupService trainSeatGroupService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String accessToken;
    private TrainSeat seat;

    @BeforeEach
    void setUp() {
        // 일단 테스트를 하려면 DB에 샘플 좌석, 샘플 유저가 존재해야 함.
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

        accessToken = jwtTokenProvider.createToken(user.getProviderId(), null, TokenType.ACCESS);

        //테스트할 좌석 생성
        seat = trainSeatGroupService.createGroupsOf("2222", "2222").stream().findFirst()
                .orElseThrow(EntityNotFoundException::new)
                .getTrainSeat().get(0);
    }

    @Test
    void testReserveSeat() throws Exception {
        // TODO : 좌석 예약 API 테스트
    }

    @Test
    void testReleaseSeat() throws Exception {
        // TODO : 좌석 해제 API 테스트
    }
}

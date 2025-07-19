package com.sullung2yo.seatcatcher.domain.train.controller;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.TokenProvider;
import com.sullung2yo.seatcatcher.domain.train.repository.TrainSeatGroupRepository;
import com.sullung2yo.seatcatcher.domain.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
class UserTrainSeatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainSeatGroupRepository trainSeatGroupRepository;

    @Autowired
    private TokenProvider tokenProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        User user = User.builder()
                .provider(Provider.APPLE)
                .providerId("testProviderId")
                .name("testUser")
                .credit(123L)
                .profileImageNum(ProfileImageNum.IMAGE_1)
                .build();
        userRepository.save(user);

        // AccessToken 생성
        accessToken = tokenProvider.createToken(user.getProviderId(), null, TokenType.ACCESS);
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 DB에서 사용자 삭제
        trainSeatGroupRepository.deleteAll();
        userRepository.deleteAll();
    }
}
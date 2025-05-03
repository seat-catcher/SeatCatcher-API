package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.TokenProvider;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatGroupRepository;
import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void getSeatInformationNotExists() throws Exception {
        mockMvc.perform(get("/user/seats")
                        .header("Authorization", "Bearer " + accessToken)
                        .queryParam("trainCode", "1234")
                        .queryParam("carCode", "2222")) // 2222 -> 222 -> 36663 타입
                .andDo(print())
                .andExpect(status().isOk())
                // 응답 전체가 배열 [ … ]
                .andExpect(jsonPath("$").isArray())
                // seatGroup 총 5개 있어야함
                .andExpect(jsonPath("$.length()").value(5))
                // 첫 번째 그룹은 ELDERLY 6석
                .andExpect(jsonPath("$[0].seatStatus.length()").value(6))
                .andExpect(jsonPath("$[0].seatStatus[0].seatType").value("ELDERLY"))
                // 두 번째 그룹은 NORMAL 12석
                .andExpect(jsonPath("$[1].seatStatus.length()").value(12));
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 DB에서 사용자 삭제
        trainSeatGroupRepository.deleteAll();
        userRepository.deleteAll();
    }
}
package com.sullung2yo.seatcatcher.train.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainCar;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.dto.request.UserTrainSeatRequest;
import com.sullung2yo.seatcatcher.train.repository.TrainCarRepository;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import com.sullung2yo.seatcatcher.train.repository.UserTrainSeatRepository;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.repository.UserTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
@Rollback
public class UserTrainSeatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private JwtTokenProviderImpl jwtTokenProvider;

    @Autowired
    private UserTrainSeatService userTrainSeatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserTagRepository userTagRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TrainCarRepository trainCarRepository;

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
        Train sampleTrain = Train.builder()
                .trainCode("test")
                .carCount(1)
                .build();
        trainRepository.save(sampleTrain);

        TrainCar sampleCar = TrainCar.builder()
                .carCode("test")
                .train(sampleTrain)
                .build();
        sampleCar.setTrain(sampleTrain);

        sampleCar.setTrainSeatGroups(new HashSet<>());

        sampleCar.getTrainSeatGroups().add(trainSeatGroupService.create(sampleCar, SeatGroupType.NORMAL_A_14));

        trainCarRepository.save(sampleCar); // cascade 로 좌석까지 모두 한꺼번에 저장됨.

        seat = sampleCar.getTrainSeatGroups().stream().findFirst()
                .orElseThrow(EntityNotFoundException::new)
                .getTrainSeats().get(0);
    }

    @Test
    void testGetSittingInfoWithToken() throws Exception {

        // When 유저의 착석 정보가 없는데 Get 을 하려고 하는 경우
        mockMvc.perform(get("/user/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                //Then
                .andExpect(status().isNoContent());

        // Given
        userTrainSeatService.create(user.getId(), seat.getId());

        // When
        mockMvc.perform(get("/user/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken))
                //Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatId").value(seat.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    void testCreateSittingInfoWithToken() throws Exception {
        //Given
        UserTrainSeatRequest request = UserTrainSeatRequest.builder()
                .seatId(seat.getId())
                .userId(user.getId())
                .build();
        //When
        mockMvc.perform(post("/user/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + accessToken))
        //then
                .andExpect(status().isCreated());


        // When
        mockMvc.perform(get("/user/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
        //Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatId").value(seat.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    void testDeleteSittingInfoWithToken() throws Exception {
        //Given
        UserTrainSeatRequest request = UserTrainSeatRequest.builder()
                .seatId(seat.getId())
                .userId(user.getId())
                .build();

        mockMvc.perform(post("/user/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + accessToken));

        // When
        mockMvc.perform(delete("/user/seats")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        )
                //Then
                .andExpect(status().isOk());

        // When 유저의 착석 정보가 없는데도 제거하려고 할 때
        mockMvc.perform(delete("/user/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                )
                //Then
                .andExpect(status().isNotFound());
    }
}

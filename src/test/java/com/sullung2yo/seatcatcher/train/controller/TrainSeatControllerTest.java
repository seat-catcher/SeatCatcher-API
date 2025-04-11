package com.sullung2yo.seatcatcher.train.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainCar;
import com.sullung2yo.seatcatcher.train.dto.request.TrainSeatRequest;
import com.sullung2yo.seatcatcher.train.repository.TrainCarRepository;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import com.sullung2yo.seatcatcher.train.service.TrainSeatService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class TrainSeatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TrainSeatService trainSeatService;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private TrainCarRepository trainCarRepository;

    @Autowired
    private TrainSeatGroupService trainSeatGroupService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long trainId;
    private Long carId;
    private Long groupId;
    private Long seatId;

    @BeforeEach
    void setUp(){
        // 우선 테스트를 해보려면 DB에 샘플 Train, 샘플 TrainCar, 샘플 TrainGroup 이 존재해야 함.
        Train sampleTrain = Train.builder()
                .trainCode("test")
                .carCount(1)
                .build();
        trainRepository.save(sampleTrain);
        trainId = sampleTrain.getId();

        TrainCar sampleCar = TrainCar.builder()
                .carCode("test")
                .train(sampleTrain)
                .build();
        sampleCar.setTrain(sampleTrain);

        sampleCar.setTrainSeatGroups(new HashSet<>());

        sampleCar.getTrainSeatGroups().add(trainSeatGroupService.create(sampleCar, SeatGroupType.NORMAL_A_14));

        trainCarRepository.save(sampleCar); // cascade 로 좌석까지 모두 한꺼번에 저장됨.

        carId = sampleCar.getId();
        groupId = sampleCar.getTrainSeatGroups().stream().findFirst()
                .orElseThrow(EntityNotFoundException::new)
                .getId();
        seatId = sampleCar.getTrainSeatGroups().stream().findFirst()
                .orElseThrow(EntityNotFoundException::new)
                .getTrainSeats().get(0).getId();
    }

    // 어떤 그룹의 모든 좌석을 끌어오는 API, 좌석 정보를 수정하는 API 두 개를 테스트해봐야 함.

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Get Group's all TrainSeat API test")
    void getAllTrainSeatAPITest() throws Exception{
        //Given

        //When & Then
        mockMvc.perform(get("/trains/{trainId}/cars/{carId}/seat-groups/{seatGroupId}/seats", trainId, carId, groupId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(14))
                .andExpect(jsonPath("$[?(@.seatLocation == 0)]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.seatLocation == 13)]").isNotEmpty());

        //Cascade 적용할 수 있게 되고 나서는 유저 정보, 경로 정보도 얻을 수 있는지 확인해야 함.
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Update Seat API test")
    void testUpdateSeatGroup() throws Exception {
        //Given
        TrainSeatRequest request = TrainSeatRequest.builder()
                .jjimCount(5)
                .build();

        //When
        mockMvc.perform(patch("/trains/{trainId}/cars/{carId}/seat-groups/{seatGroupId}/seats/{seatId}"
                        , trainId, carId, groupId, seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
        // Then
                .andExpect(status().isOk());


        //When
        mockMvc.perform(get("/trains/{trainId}/cars/{carId}/seat-groups/{seatGroupId}/seats", trainId, carId, groupId))
        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(14))
                .andExpect(jsonPath("$[?(@.jjimCount == 5)]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.jjimCount == 0)]").isNotEmpty());
    }
}

package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainCar;
import com.sullung2yo.seatcatcher.train.repository.TrainCarRepositoryForTest;
import com.sullung2yo.seatcatcher.train.repository.TrainRepositoryForTest;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class TrainSeatGroupControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private TrainCarRepositoryForTest trainCarRepository;

    @Autowired
    private TrainRepositoryForTest trainRepository;

    @Autowired
    private TrainSeatGroupService trainSeatGroupService;

    private Long trainId;
    private Long carId;

    @BeforeEach
    void setUp(){
        // 우선 테스트를 하려면 DB에 샘플 Train 과 샘플 TrainCar 가 존재해야 함.
        //TODO :: 현재는 검증되지 않은 RepositoryForTest 를 사용하여 save 를 수행합니다. 나중에는 해당 부분을 검증된 진짜 Repository로 교체해야 합니다.
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
        sampleCar.getTrainSeatGroups().add(trainSeatGroupService.create(sampleCar, SeatGroupType.NORMAL_B_14));
        sampleCar.getTrainSeatGroups().add(trainSeatGroupService.create(sampleCar, SeatGroupType.NORMAL_C_14));
        sampleCar.getTrainSeatGroups().add(trainSeatGroupService.create(sampleCar, SeatGroupType.ELDERLY_A));
        sampleCar.getTrainSeatGroups().add(trainSeatGroupService.create(sampleCar, SeatGroupType.ELDERLY_B));

        trainCarRepository.save(sampleCar);
        carId = sampleCar.getId();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Get TrainCar's all TrainSeatGroups API test")
    void testGetAllTrainSeatGroups() throws Exception {
        //Given
            // @BeforeEach 에서 이미 정보를 다 줬음. 이 부분은 생략.

        //When & Then
        mockMvc.perform(get("/trains/{trainId}/cars/{carId}/seat-groups", trainId, carId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(5)) // 5개를 만들었기 때문에 배열 크기가 5개인지 확인해야 함.
                .andExpect(jsonPath("$[?(@.groupType == 'NORMAL_A_14')]").exists())
                .andExpect(jsonPath("$[?(@.groupType == 'NORMAL_B_14')]").exists())
                .andExpect(jsonPath("$[?(@.groupType == 'NORMAL_C_14')]").exists())
                .andExpect(jsonPath("$[?(@.groupType == 'ELDERLY_A')]").exists())
                .andExpect(jsonPath("$[?(@.groupType == 'ELDERLY_B')]").exists());
    }
}

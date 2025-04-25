package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.dto.request.TrainSeatRequest;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class TrainSeatServiceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private TrainSeatRepository trainSeatRepository;
    @Autowired
    private TrainSeatService trainSeatService;

    private Train sampleTrain;
    @Autowired
    private TrainSeatGroupService trainSeatGroupService;

    @BeforeEach
    void setUp() {
        sampleTrain = trainSeatGroupService.createGroupsOf("2222", "2222").get(0);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Modify Seat info test")
    void modifySeatInfoTest() throws Exception {
        //Given
        TrainSeat original = sampleTrain.getTrainSeat().get(0);

        //When
        trainSeatService.update(original.getId(), TrainSeatRequest.builder()
                .seatType(SeatType.PREGNANT)
                .build());
        TrainSeat modified = trainSeatService.findById(original.getId());

        //Then
        Assertions.assertNotNull(trainSeatRepository.findById(original.getId()));
        Assertions.assertEquals(SeatType.PREGNANT, modified.getSeatType());
    }
}

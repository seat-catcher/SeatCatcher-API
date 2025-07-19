package com.sullung2yo.seatcatcher.domain.train.controller;
import com.sullung2yo.seatcatcher.domain.train.service.TrainSeatGroupService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class TrainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private TrainSeatGroupService groupService;

    private String trainCode;
    private String carCode;

    @BeforeEach
    void setUp(){
        // 우선 테스트를 하려면 DB에 샘플 TrainSeatGroup 과 샘플 TrainCar 가 존재해야 함.
        trainCode = "2222";
        carCode = "2222";

        groupService.createGroupsOf(trainCode, carCode);
    }

    // /trains/incomings 테스트 필요
}

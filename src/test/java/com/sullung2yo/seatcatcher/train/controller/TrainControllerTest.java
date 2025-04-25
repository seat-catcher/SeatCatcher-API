package com.sullung2yo.seatcatcher.train.controller;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        // 우선 테스트를 하려면 DB에 샘플 Train 과 샘플 TrainCar 가 존재해야 함.
        trainCode = "2222";
        carCode = "2222";

        groupService.createGroupsOf(trainCode, carCode);
    }

    // /trains/incomings 테스트 필요
}

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
public class TrainSeatGroupControllerTest {

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

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Get TrainCar's all TrainSeatGroups API test")
    void testGetAllTrainSeatGroups() throws Exception {
        //Given
            // @BeforeEach 에서 이미 정보를 다 줬음. 이 부분은 생략.

        //When & Then
        // 이미 존재하는 열차, 차량에 대해서 조회를 했을 경우
        mockMvc.perform(get("/trains/{trainCode}/cars/{carCode}/seat-groups", trainCode, carCode))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        // 나머지 정보는 굳이 확인할 필요는 없음. Service 가 신뢰 가능하다는 가정 하에.

        //When & Then
        // 존재하지 않는 열차에 대해서 조회를 시도했을 경우 ( 36663 )
        mockMvc.perform(get("/trains/{trainCode}/cars/{carCode}/seat-groups", "2202", "2222"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    // 추가로 타입에 맞게 잘 생성됐는지 두 번 확인해줘야 함.
                ;

        //When & Then
        // 존재하지 않는 열차에 대해서 조회를 시도했을 경우 ( 37773 ) ::TODO:: 매핑 테이블 완성되면 이 부분 다시 테스트해봐야 합니다.
        mockMvc.perform(get("/trains/{trainCode}/cars/{carCode}/seat-groups", "2203", "2222"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        // 추가로 타입에 맞게 잘 생성됐는지 두 번 확인해줘야 함.
        ;
    }
}

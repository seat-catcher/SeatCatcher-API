package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.*;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatGroupRepository;
import com.sullung2yo.seatcatcher.train.utility.SeatInfoResponseAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TrainSeatGroupServiceTest {

    @Autowired
    private TrainSeatGroupService service;

    @Autowired
    private TrainSeatGroupRepository trainSeatGroupRepository;

    @Autowired
    private SeatInfoResponseAssembler seatInfoResponseAssembler;


    @BeforeEach
    void setUp() {
        service = new TrainSeatGroupServiceImpl(trainSeatGroupRepository, seatInfoResponseAssembler);
    }

    @Test
    @DisplayName("열차 코드와 차량 코드로 그룹을 찾거나 생성하는 기능 테스트")
    void testFindOrCreateByTrainCodeAndCarCode() {
        // given
        String trainCode = "1234";
        String carCode = "7120";

        TrainSeatGroup mockTrainSeatGroup = new TrainSeatGroup();
        mockTrainSeatGroup.setTrainCode(trainCode);
        mockTrainSeatGroup.setCarCode(carCode);

        List<TrainSeatGroup> trainSeatGroups = List.of(mockTrainSeatGroup);

        // when
        List<TrainSeatGroup> result = service.findAllByTrainCodeAndCarCode(trainCode, carCode);

        // then
        // 최초 실행 시 Train이 없으므로 빈 리스트가 반환되어야 함
        assertTrue(result.isEmpty());

        // when
        // 다시 동일한 trainCode와 carCode로 TrainSeatGroup 만들고 실행하면, TrainSeatGroup이 반환되어야 함
        service.createGroupsOf(trainCode, carCode);
        result = service.findAllByTrainCodeAndCarCode(trainCode, carCode); // DB에는 없는걸로 질의

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(trainCode, result.get(0).getTrainCode());
        assertEquals(carCode, result.get(0).getCarCode());
    }

    @Test
    @DisplayName("Normal A, B, C 중 하나로 그룹을 만들었을 경우를 테스트")
    void testCreateGroup_normal()
    {
        //when
        TrainSeatGroup group = service.createTrainSeatGroup("2204","2111", SeatGroupType.NORMAL_A_14);

        //then
        assertNotNull(group);
        assertEquals("2204", group.getTrainCode());
        assertEquals("2111", group.getCarCode());
        assertEquals(SeatGroupType.NORMAL_A_14, group.getSeatGroupType()); // 2204 -> 204 -> 37773 타입

        List<TrainSeat> seats = group.getTrainSeat();
        assertEquals(group.getSeatGroupType().getSeatCount(), seats.size());

        for(int i = 0; i < seats.size(); i++)
        {
            TrainSeat seat = seats.get(i);
            assertNotNull(seat);
            assertEquals(group, seat.getTrainSeatGroup());
            assertEquals(i, seat.getSeatLocation());
            assertEquals(SeatType.NORMAL, seat.getSeatType());
        }
    }

    @Test
    @DisplayName("Elderly A, B 중 하나로 그룹을 만들었을 경우를 테스트")
    void testCreateGroup_elderly()
    {
        //when
        TrainSeatGroup group = service.createTrainSeatGroup("2204","2111", SeatGroupType.ELDERLY_A);

        //then
        assertNotNull(group);
        assertEquals("2204", group.getTrainCode());
        assertEquals("2111", group.getCarCode());
        assertEquals(SeatGroupType.ELDERLY_A, group.getSeatGroupType());

        List<TrainSeat> seats = group.getTrainSeat();
        assertEquals(group.getSeatGroupType().getSeatCount(), seats.size());

        for(int i = 0; i < seats.size(); i++)
        {
            TrainSeat seat = seats.get(i);
            assertNotNull(seat);
            assertEquals(group, seat.getTrainSeatGroup());
            assertEquals(i, seat.getSeatLocation());
            assertEquals(SeatType.ELDERLY, seat.getSeatType());
        }
    }
}

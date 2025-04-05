package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

public class TrainSeatGroupServiceTest {

    private TrainSeatGroupService service;

    @BeforeEach
    void setUp() {
        service = new TrainSeatGroupServiceImpl();
    }

    @Test
    @DisplayName("Normal A, B, C 중 하나로 그룹을 만들었을 경우를 테스트")
    void testCreateGroup_normal()
    {
        //given
        TrainCar dummyCar = TrainCar.builder()
                .trainSeatGroups(new HashSet<>())
                .carCode("7208")
                .train(null)
                .build();

        //when
        TrainSeatGroup group = service.create(dummyCar, SeatGroupType.NORMAL_A_14);

        //then
        Assertions.assertNotNull(group);
        Assertions.assertEquals(dummyCar, group.getTrainCar());
        Assertions.assertEquals(SeatGroupType.NORMAL_A_14, group.getType());

        List<TrainSeat> seats = group.getTrainSeats();
        Assertions.assertEquals(group.getType().getSeatCount(), seats.size());

        for(int i = 0; i < seats.size(); i++)
        {
            TrainSeat seat = seats.get(i);
            Assertions.assertNotNull(seat);
            Assertions.assertEquals(group, seat.getTrainSeatGroup());
            Assertions.assertEquals(i, seat.getSeatLocation());
            Assertions.assertEquals(0, seat.getJjimCount());
            Assertions.assertEquals(SeatType.NORMAL, seat.getSeatType());
        }
    }

    @Test
    @DisplayName("Elderly A, B 중 하나로 그룹을 만들었을 경우를 테스트")
    void testCreateGroup_elderly()
    {
        //given
        TrainCar dummyCar = TrainCar.builder()
                .trainSeatGroups(new HashSet<>())
                .carCode("7208")
                .train(null)
                .build();

        //when
        TrainSeatGroup group = service.create(dummyCar, SeatGroupType.ELDERLY_A);

        //then
        Assertions.assertNotNull(group);
        Assertions.assertEquals(dummyCar, group.getTrainCar());
        Assertions.assertEquals(SeatGroupType.ELDERLY_A, group.getType());

        List<TrainSeat> seats = group.getTrainSeats();
        Assertions.assertEquals(group.getType().getSeatCount(), seats.size());

        for(int i = 0; i < seats.size(); i++)
        {
            TrainSeat seat = seats.get(i);
            Assertions.assertNotNull(seat);
            Assertions.assertEquals(group, seat.getTrainSeatGroup());
            Assertions.assertEquals(i, seat.getSeatLocation());
            Assertions.assertEquals(0, seat.getJjimCount());
            Assertions.assertEquals(SeatType.NORMAL, seat.getSeatType());
        }
    }
}

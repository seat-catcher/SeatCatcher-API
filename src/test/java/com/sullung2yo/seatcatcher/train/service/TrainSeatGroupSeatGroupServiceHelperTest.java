package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.SeatType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TrainSeatGroupSeatGroupServiceHelperTest {

    private TrainHelperService service;

    @Mock
    private TrainRepository trainRepository;

    @BeforeEach
    void setUp(){
        service = new TrainHelperServiceImpl(trainRepository);
    }

    @Test
    @DisplayName("Normal A, B, C 중 하나로 그룹을 만들었을 경우를 테스트")
    void testCreateGroup_normal()
    {
        //when
        TrainSeatGroup group = service.create("2204","2111", SeatGroupType.NORMAL_A_14);

        //then
        Assertions.assertNotNull(group);
        Assertions.assertEquals("2204", group.getTrainCode());
        Assertions.assertEquals("2111", group.getCarCode());
        Assertions.assertEquals(SeatGroupType.NORMAL_A_14, group.getType());

        List<TrainSeat> seats = group.getTrainSeat();
        Assertions.assertEquals(group.getType().getSeatCount(), seats.size());

        for(int i = 0; i < seats.size(); i++)
        {
            TrainSeat seat = seats.get(i);
            Assertions.assertNotNull(seat);
            Assertions.assertEquals(group, seat.getTrainSeatGroup());
            Assertions.assertEquals(i, seat.getSeatLocation());
            Assertions.assertEquals(SeatType.NORMAL, seat.getSeatType());
        }
    }

    @Test
    @DisplayName("Elderly A, B 중 하나로 그룹을 만들었을 경우를 테스트")
    void testCreateGroup_elderly()
    {
        //when
        TrainSeatGroup group = service.create("2204","2111", SeatGroupType.ELDERLY_A);

        //then
        Assertions.assertNotNull(group);
        Assertions.assertEquals("2204", group.getTrainCode());
        Assertions.assertEquals("2111", group.getCarCode());
        Assertions.assertEquals(SeatGroupType.ELDERLY_A, group.getType());

        List<TrainSeat> seats = group.getTrainSeat();
        Assertions.assertEquals(group.getType().getSeatCount(), seats.size());

        for(int i = 0; i < seats.size(); i++)
        {
            TrainSeat seat = seats.get(i);
            Assertions.assertNotNull(seat);
            Assertions.assertEquals(group, seat.getTrainSeatGroup());
            Assertions.assertEquals(i, seat.getSeatLocation());
            Assertions.assertEquals(SeatType.ELDERLY, seat.getSeatType());
        }
    }
}

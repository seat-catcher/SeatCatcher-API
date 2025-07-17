package com.sullung2yo.seatcatcher.domain.train.service;

import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeat;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import com.sullung2yo.seatcatcher.domain.train.enums.SeatGroupType;
import com.sullung2yo.seatcatcher.domain.train.enums.SeatType;
import com.sullung2yo.seatcatcher.domain.train.repository.TrainSeatGroupRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TrainSeatGroupServiceTest {

    @Autowired
    private TrainSeatGroupService service;

    @Autowired
    private TrainSeatGroupRepository trainSeatGroupRepository;

    @AfterEach
    void tearDown() {
        trainSeatGroupRepository.deleteAll();
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

    @ParameterizedTest
    @MethodSource("seatGroupTypeProvider")
    @DisplayName("다양한 좌석 타입으로 그룹 만드는 테스트")
    void testCreateGroupType14(SeatGroupType seatGroupType, SeatType expectedSeatType) {
        // when
        TrainSeatGroup group = service.createTrainSeatGroup("2204", "2111", seatGroupType);

        // then
        assertNotNull(group);
        assertEquals("2204", group.getTrainCode());
        assertEquals("2111", group.getCarCode());
        assertEquals(seatGroupType, group.getSeatGroupType());

        List<TrainSeat> seats = group.getTrainSeat();
        assertEquals(group.getSeatGroupType().getSeatCount(), seats.size());

        for (int i = 0; i < seats.size(); i++) {
            TrainSeat seat = seats.get(i);
            assertNotNull(seat);
            assertEquals(group, seat.getTrainSeatGroup());
            assertEquals(i, seat.getSeatLocation());
            assertEquals(expectedSeatType, seat.getSeatType());
        }
    }

    // 테스트 파라미터 제공자
    static Stream<Arguments> seatGroupTypeProvider() {
        return Stream.of(
                Arguments.of(SeatGroupType.NORMAL_A_14, SeatType.NORMAL),
                Arguments.of(SeatGroupType.NORMAL_B_14, SeatType.NORMAL),
                Arguments.of(SeatGroupType.NORMAL_C_14, SeatType.NORMAL),
                Arguments.of(SeatGroupType.NORMAL_A_12, SeatType.NORMAL),
                Arguments.of(SeatGroupType.NORMAL_B_12, SeatType.NORMAL),
                Arguments.of(SeatGroupType.NORMAL_C_12, SeatType.NORMAL),
                Arguments.of(SeatGroupType.PRIORITY_A, SeatType.PRIORITY),
                Arguments.of(SeatGroupType.PRIORITY_B, SeatType.PRIORITY)
        );
    }
}
package com.sullung2yo.seatcatcher.train.utility;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.dto.response.SeatStatus;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatGroupRepository;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest
class SeatStatusAssemblerTest {

    @Autowired
    private SeatStatusAssembler seatStatusAssembler;

    @Autowired
    private TrainSeatGroupService trainSeatGroupService;

    @Autowired
    private TrainSeatGroupRepository trainSeatGroupRepository;

    @Autowired
    private EntityManager entityManager;

    private TrainSeatGroup seatGroups;

    @BeforeEach
    void setUp() {
        // createTrainSeatGroup 메서드로 테스트 데이터 생성
        seatGroups = trainSeatGroupService.createTrainSeatGroup("1234", "7018", SeatGroupType.NORMAL_A_12);
        entityManager.flush();
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 삭제
        trainSeatGroupRepository.delete(seatGroups);
        entityManager.flush();
    }

    @Test
    void assembleSeatResponse() {
        // Given
        // 테스트 데이터가 이미 생성되어 있음

        // When
        List<SeatStatus> result = seatStatusAssembler.assembleSeatResponse(seatGroups);

        // Then
        assertNotNull(result);
        assertEquals(12, result.size());

        for (int i = 0; i < 12; i++) {
            SeatStatus seat = result.get(i);
            assertNotNull(seat.getSeatId());
            assertNotNull(seat.getSeatLocation());
            assertNotNull(seat.getSeatType().name());
            assertNull(seat.getOccupant()); // occupant는 처음 TrainSeatGroup 생성 시 null로 설정되어서 null 체크
        }
    }
}
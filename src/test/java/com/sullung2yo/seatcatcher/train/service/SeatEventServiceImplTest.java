package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.domain.path_history.service.PathHistoryService;
import com.sullung2yo.seatcatcher.domain.train.enums.SeatGroupType;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.domain.alarm.service.UserAlarmService;
import com.sullung2yo.seatcatcher.domain.train.service.SeatEventServiceImpl;
import com.sullung2yo.seatcatcher.domain.train.service.TrainSeatGroupService;
import com.sullung2yo.seatcatcher.domain.train.service.UserTrainSeatService;
import com.sullung2yo.seatcatcher.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when; // ✅ 올바른 import

@ExtendWith(MockitoExtension.class)
class SeatEventServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserService userService;

    @Mock
    private UserTrainSeatService userTrainSeatService;

    @Mock
    private UserAlarmService userAlarmService;

    @Mock
    private PathHistoryService pathHistoryService;

    @Mock
    private SimpMessagingTemplate webSocketMessagingTemplate;

    @Mock
    private TrainSeatGroupService trainSeatGroupService;

    @InjectMocks
    private SeatEventServiceImpl seatEventService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(seatEventService, "exchangeName", "test");
        TrainSeatGroup trainSeatGroup = TrainSeatGroup.builder()
                .trainCode("1234")
                .carCode("7012")
                .seatGroupType(SeatGroupType.NORMAL_A_14)
                .trainSeat(new ArrayList<>())
                .build();
        List<TrainSeatGroup> trainSeatGroups = List.of(trainSeatGroup);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testPublishSeatEvent() {
        // Given
        // TrainCode, CarCode가 주어졌을 때
        TrainSeatGroup trainSeatGroup = TrainSeatGroup.builder()
                .trainCode("1234")
                .carCode("7012")
                .seatGroupType(SeatGroupType.NORMAL_A_14)
                .trainSeat(new ArrayList<>())
                .build();
        List<TrainSeatGroup> trainSeatGroups = List.of(trainSeatGroup);

        SeatInfoResponse response = SeatInfoResponse.builder()
                .trainCode("1234")
                .carCode("7012")
                .seatGroupType(SeatGroupType.NORMAL_A_14)
                .seatStatus(new ArrayList<>())
                .build();
        List<SeatInfoResponse> seatInfoResponses = List.of(response);
        when(trainSeatGroupService.findAllByTrainCodeAndCarCode(any(String.class), any(String.class)))
                .thenReturn(trainSeatGroups);
        when(trainSeatGroupService.createSeatInfoResponse(
                any(String.class), any(String.class), ArgumentMatchers.<List<TrainSeatGroup>>any()))
                .thenReturn(seatInfoResponses);

        // When
        seatEventService.publishSeatEvent("1234", "7012");

        // Then
        // RabbitTemplate의 convertAndSend 메서드가 호출되었는지 확인
        Mockito.verify(rabbitTemplate, Mockito.times(1))
                .convertAndSend(eq("test"), any(String.class), eq(seatInfoResponses));
    }

    @Test
    void testHandleSeatEvent() {
        // Given
        SeatInfoResponse response = SeatInfoResponse.builder()
                .trainCode("1234")
                .carCode("7012")
                .seatGroupType(SeatGroupType.NORMAL_A_14)
                .seatStatus(new ArrayList<>())
                .build();
        List<SeatInfoResponse> seatInfoResponses = List.of(response);

        // When
        seatEventService.handleSeatEvent(seatInfoResponses);

        // Then
        Mockito.verify(webSocketMessagingTemplate, Mockito.times(1))
                .convertAndSend(any(String.class), eq(seatInfoResponses));
    }

    @Test
    void publishSeatYieldRequestEvent() {
        // TODO :: 좌석 양보 관련 이벤트 테스트 코드 작성 필요
        // publishSeatYieldEvent -> handleYieldEvent 호출 과정 테스트
    }

    @Test
    void publishSeatYieldCancelEvent() {
        // TODO :: 좌석 양보 취소 관련 이벤트 테스트 코드 작성 필요
        // publishSeatYieldCancelEvent -> handleYieldCancelEvent 호출 과정 테스트
    }

    @Test
    void publishSeatYieldAcceptEvent() {
        // TODO :: 좌석 양보 수락 관련 이벤트 테스트 코드 작성 필요
        // publishSeatYieldAcceptEvent -> handleYieldAcceptEvent 호출 과정 테스트
    }

    @Test
    void publishSeatYieldRejectEvent() {
        // TODO :: 좌석 양보 거절 관련 이벤트 테스트 코드 작성 필요
        // publishSeatYieldRejectEvent -> handleYieldRejectEvent 호출 과정 테스트
    }

    @Test
    void invalidReuqestType() {
        // TODO :: 잘못된 요청 타입에 대한 테스트 코드 작성 필요
    }
}
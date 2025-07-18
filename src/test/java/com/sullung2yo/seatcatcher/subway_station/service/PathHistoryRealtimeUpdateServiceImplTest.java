package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.common.service.TaskScheduleService;
import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.repository.PathHistoryRepository;
import com.sullung2yo.seatcatcher.train.domain.TrainArrivalState;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import com.sullung2yo.seatcatcher.train.service.SeatEventService;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@Transactional // 테스트 이후 롤백되게 함
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2 설정 유지
@ExtendWith(MockitoExtension.class)
public class PathHistoryRealtimeUpdateServiceImplTest {

    @Mock
    private SubwayStationService mockSubwayStationService;

    @Mock
    private PathHistoryRepository mockPathHistoryRepository;

    @Mock
    private PathHistoryEventService mockPathHistoryEventService;

    @Mock
    private TrainSeatGroupService mockTrainSeatGroupService;

    @Mock
    private TaskScheduleService mockScheduleService;

    @Mock
    private SeatEventService mockSeatEventService;

    @InjectMocks
    private PathHistoryRealtimeUpdateServiceImpl pathHistoryRealtimeUpdateServiceWithMock;



    ///=============================== 실시간 도착 정보를 통해 PathHistory 갱신 로직 테스트 =================================///
    @Test
    @DisplayName("Test that updateArrivalTimeAndSchedule send websocket message and schedule properly")
    void updateArrivalTimeAndScheduleTest() {
        /* 함수의 흐름 :
                1. pathHistory 에 User 가 없으면 에러 처리.
                2. pathHistory 에서 expectedArrivalTime 을 이용해서 계산.
                        ㄴ Mock 할 PathHistory 는 User, ExpectedArrivalTime 이 있어야만 함.
                3. fetchIncomingTrainsResponseByPathHistory 메소드를 호출하여 IncomingTrainsResponse 를 얻어냄.
                        ㄴ 그냥 null 반환하게 해도 좋음.
                4. processTrainStateAndRefresh 함수를 사용하여 isArrived 를 얻어냄.
                        ㄴ 그냥 false 반환하게 해도 좋음.
                5. pathHistory 에서 남은 시간을 계산, 이를 바탕으로 다음에 스케줄링할 시간을 알아냄.
                6. 그 시간을 기반으로 스케줄링
                7. publishPathHistoryEvent 서비스를 호출함.

                테스트해보고 싶은 것 :
                    정상적으로 다음 스케줄링을 진행하는지
                    정상적으로 publishPathHistoryEvent를 호출하는지
         */

        // given
        User user = User.builder()
                .provider(Provider.APPLE)
                .providerId("testUser")
                .role(UserRole.ROLE_USER)
                .name("테스터")
                .credit(0L)
                .build();

        PathHistory pathHistory = PathHistory.builder()
                .user(user)
                .expectedArrivalTime(LocalDateTime.now().plusMinutes(30))
                .build(); // 예상 도착 시간은 30분 뒤.
        pathHistory.setId(1L);

        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);

        doNothing().when(mockPathHistoryEventService).publishPathHistoryEvent(any(), any(), anyBoolean());
        doReturn(null).when(mockScheduleService).runThisAtBeforeSeconds(any(), anyLong(), any(Runnable.class));
        doReturn(null).when(spyService).fetchIncomingTrainsResponseByPathHistory(any(PathHistory.class), anyString());
        doReturn(false).when(spyService).processTrainStateAndRefresh(anyInt(), any(TrainArrivalState.class), any(PathHistory.class), anyLong(), anyLong(), anyLong());

        // when
        spyService.updateArrivalTimeAndSchedule(pathHistory, "1234", TrainArrivalState.STATE_NOT_FOUND);

        // then
        verify(mockPathHistoryEventService).publishPathHistoryEvent(anyLong(), any(), anyBoolean());
        verify(mockScheduleService).runThisAtBeforeSeconds(any(LocalDateTime.class), anyLong(), any(Runnable.class));
    }


    @Test
    @DisplayName("SubwayStationService 가 유효한 Response 를 반환하는 경우 null 이 아닌 자신이 추적중인 train 의 정보를 함수가 반환함을 테스트")
    void fetchIncomingTrainsResponseByPathHistory_shouldReturnResponse_whenTrainCodeMatches()
    {
        // given
        SubwayStation start = SubwayStation.builder()
                .stationName("출발")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(100)
                .build();

        SubwayStation end = SubwayStation.builder()
                .stationName("도착")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(180)
                .build();

        PathHistory pathHistory = PathHistory.builder()
                .startStation(start)
                .endStation(end)
                .build();

        String trainCode = "1234";
        String dummyJson = "{}";

        IncomingTrainsResponse expectedResponse = IncomingTrainsResponse.builder()
                .subwayId("1234")
                .build();

        // when
        Mockito.when(mockSubwayStationService.fetchIncomingTrains("2", "도착")).thenReturn(Optional.of(dummyJson));
        Mockito.when(mockSubwayStationService.parseIncomingResponse(eq("2"), eq(start), eq(end), eq(dummyJson)))
                .thenReturn(List.of(expectedResponse));

        // then
        IncomingTrainsResponse myResponse = pathHistoryRealtimeUpdateServiceWithMock.fetchIncomingTrainsResponseByPathHistory(pathHistory, trainCode);
        assertThat(myResponse).isNotNull();
        assertThat(myResponse.getSubwayId()).isEqualTo("1234");
    }

    @Test
    @DisplayName("열차가 연속으로 발견되지 않았을 때 TrainArrivalState 분기가 잘 이루어지는지 테스트")
    void processTrainState_whenTrainNotFoundTwice()
    {
        // isArrived 는 False 여야 하며, 하차 처리가 일어나서는 안 된다!
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_NOT_FOUND.getStateCode(),
                TrainArrivalState.STATE_NOT_FOUND,
                new PathHistory(),
                -1, 120, 40
        );

        //then
        assertThat(isArrived).isFalse();
        verify(spyService, never()).automaticDropOff(any());
    }

    @Test
    @DisplayName("전에 열차가 발견됐었는데 이번엔 발견되지 않았을 때 TrainArrivalState 분기가 잘 이루어지는지 테스트")
    void processTrainState_whenTrainWasFoundBeforeButNowNotFound()
    {
        // isArrived 는 True 여야 하며, 하차 처리가 일어나야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).automaticDropOff(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_NOT_FOUND.getStateCode(),
                TrainArrivalState.STATE_ARRIVED,
                new PathHistory(),
                -1, 120, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).automaticDropOff(any());
    }

    @Test
    @DisplayName("열차가 ARRIVED 상태인 경우 TrainArrivalState 분기가 잘 이루어지는지 테스트")
    void processTrainState_whenTrainIsArrived()
    {
        // ARRIVED , ENTERING , DEPARTED 이 세 가지 경우에 대해서는 하차 처리가 잘 수행되어야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).automaticDropOff(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_ARRIVED.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                10, 30, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).automaticDropOff(any());
    }

    @Test
    @DisplayName("열차가 ENTERING 상태인 경우 TrainArrivalState 분기가 잘 이루어지는지 테스트2")
    void processTrainState_whenTrainIsEntering()
    {
        // ARRIVED , ENTERING , DEPARTED 이 세 가지 경우에 대해서는 하차 처리가 잘 수행되어야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).automaticDropOff(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_ENTERING.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                10, 30, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).automaticDropOff(any());
    }

    @Test
    @DisplayName("열차가 DEPARTED 상태인 경우 TrainArrivalState 분기가 잘 이루어지는지 테스트3")
    void processTrainState_whenTrainIsDeparted()
    {
        // ARRIVED , ENTERING , DEPARTED 이 세 가지 경우에 대해서는 하차 처리가 잘 수행되어야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).automaticDropOff(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_DEPARTED.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                10, 30, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).automaticDropOff(any());
    }

    @Test
    @DisplayName("열차가 이전역 진입 중이며 오차가 적음에도 realtime 계산값보다 expected 계산값이 적다면 갱신되는지 테스트")
    void processTrainState_whenTrainIsEnteringButRealtimeValIsSmallerThanExpected()
    {
        //when
        when(mockPathHistoryRepository.save(any(PathHistory.class)))
                .thenReturn(new PathHistory());
        //doNothing().when(mockPathHistoryEventService).publishPathHistoryEvent(nullable(Long.class)); // 여기서 호출 안 하게 바뀜.
        when(mockTrainSeatGroupService.getSittingTrainCarInfo(nullable(User.class)))
                .thenReturn(null);

        boolean isArrived = pathHistoryRealtimeUpdateServiceWithMock.processTrainStateAndRefresh(
                TrainArrivalState.STATE_PRVSTTN_ENTERING.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                110, 100, 40 // 오차 10초. 원래라면 갱신은 일어나면 안 됨.
        );

        //then
        assertThat(isArrived).isFalse();
        verify(mockPathHistoryRepository).save(any());
    }

    @Test
    @DisplayName("realtime 계산값과 expected 계산값 사이에 오차가 매우 크면 갱신되는지 테스트")
    void processTrainState_whenDifferenceIsTooMuch()
    {
        //when
        when(mockPathHistoryRepository.save(any(PathHistory.class)))
                .thenReturn(new PathHistory());
        // doNothing().when(mockPathHistoryEventService).publishPathHistoryEvent(nullable(Long.class)); // 여기서 호출 안 하게 바뀜.
        when(mockTrainSeatGroupService.getSittingTrainCarInfo(nullable(User.class)))
                .thenReturn(null);

        boolean isArrived = pathHistoryRealtimeUpdateServiceWithMock.processTrainStateAndRefresh(
                TrainArrivalState.STATE_PRVSTTN_ENTERING.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                110, 190, 40 // 오차 80초. 갱신이 일어나야 함.
        );

        //then
        assertThat(isArrived).isFalse();
        verify(mockPathHistoryRepository).save(any());
    }

    @Test
    @DisplayName("realtime 계산값과 expected 계산값 사이에 오차가 작다면 갱신되지 않는지 테스트")
    void processTrainState_whenDifferenceIsSmall()
    {
        //when
        boolean isArrived = pathHistoryRealtimeUpdateServiceWithMock.processTrainStateAndRefresh(
                TrainArrivalState.STATE_PRVSTTN_ENTERING.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                110, 120, 40 // 오차 10초. 갱신이 일어나면 안 됨.
        );

        //then
        assertThat(isArrived).isFalse();
        verify(mockPathHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getRealtimeRemainingSeconds 에서 만약 정상적으로 값을 반환받을 수 있다면 그 값으로 바로 리턴을 해주는지 테스트")
    void getRealtimeRemainingSeconds_whenAPIReturnValidArrivalTime()
    {
        // given
        IncomingTrainsResponse response = IncomingTrainsResponse.builder()
                .arrivalCode(0)
                .arrivalTime("120")
                .build();
        PathHistory pathHistory = new PathHistory();

        //when
        long realtimeRemainingSeconds = pathHistoryRealtimeUpdateServiceWithMock.getRealtimeRemainingSeconds(response, pathHistory);

        //then
        assertThat(realtimeRemainingSeconds).isEqualTo(120);
    }

    @Test
    @DisplayName("getRealtimeRemainingSeconds 에서 ArrivalCode 가 99일 경우 제대로 값을 리턴해주는지 테스트")
    void getRealtimeRemainingSeconds_whenResponsesArrivalCodeIs99()
    {
        // given
        IncomingTrainsResponse response = IncomingTrainsResponse.builder()
                .arrivalTime("0")
                .arrivalCode(TrainArrivalState.STATE_DRIVING.getStateCode())
                .arrivalMessage("12분 후(고속터미널)")
                .build();
        PathHistory pathHistory = new PathHistory();

        //when
        long realtimeRemainingSeconds = pathHistoryRealtimeUpdateServiceWithMock.getRealtimeRemainingSeconds(response, pathHistory);

        //then
        assertThat(realtimeRemainingSeconds).isEqualTo(12 * 60);
    }

    @Test
    @DisplayName("getRealtimeRemainingSeconds 에서 ArrivalCode 가 전역 뭐시기랑 관련이 있는 경우 제대로 값을 리턴해주는지 테스트")
    void getRealtimeRemainingSeconds_whenResponsesArrivalCodeIsRelatedToPRVSTTN()
    {
        // given
        IncomingTrainsResponse response = IncomingTrainsResponse.builder()
                .arrivalTime("0")
                .arrivalCode(TrainArrivalState.STATE_PRVSTTN_ARRIVED.getStateCode())
                .build();

        SubwayStation start = SubwayStation.builder()
                .stationName("출발")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(100)
                .build();

        SubwayStation end = SubwayStation.builder()
                .stationName("도착")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(180)
                .build();

        PathHistory pathHistory = PathHistory.builder()
                .startStation(start)
                .endStation(end)
                .build();

        //when
        when(mockSubwayStationService.getPreviousStation(start, end)).thenReturn(start);
        when(mockSubwayStationService.calculateRemainingSeconds(start, end)).thenReturn(80L);
        long realtimeRemainingSeconds = pathHistoryRealtimeUpdateServiceWithMock.getRealtimeRemainingSeconds(response, pathHistory);

        //then
        assertThat(realtimeRemainingSeconds).isEqualTo(80L);
    }

    @Test
    @DisplayName("getRealtimeRemainingSeconds 에서 진입중일 때 60을 반환해주는지 테스트")
    void getRealtimeRemainingSeconds_whenResponsesArrivalCodeIsENTERING()
    {
        // given
        IncomingTrainsResponse response = IncomingTrainsResponse.builder()
                .arrivalTime("0")
                .arrivalCode(TrainArrivalState.STATE_ENTERING.getStateCode())
                .build();
        PathHistory pathHistory = new PathHistory();

        //when
        long realtimeRemainingSeconds = pathHistoryRealtimeUpdateServiceWithMock.getRealtimeRemainingSeconds(response, pathHistory);

        //then
        assertThat(realtimeRemainingSeconds).isEqualTo(60);
    }

    @Test
    @DisplayName("getRealtimeRemainingSeconds 에서 진입중일 때 60을 반환해주는지 테스트")
    void getRealtimeRemainingSeconds_whenResponsesArrivalCodeIsARRIVED()
    {
        // given
        IncomingTrainsResponse response = IncomingTrainsResponse.builder()
                .arrivalTime("0")
                .arrivalCode(TrainArrivalState.STATE_ARRIVED.getStateCode())
                .build();
        PathHistory pathHistory = new PathHistory();

        //when
        long realtimeRemainingSeconds = pathHistoryRealtimeUpdateServiceWithMock.getRealtimeRemainingSeconds(response, pathHistory);

        //then
        assertThat(realtimeRemainingSeconds).isEqualTo(0);
    }



    /// =============================== 실시간 도착 정보를 통해 PathHistory 갱신 로직 테스트 끝 =================================///



}

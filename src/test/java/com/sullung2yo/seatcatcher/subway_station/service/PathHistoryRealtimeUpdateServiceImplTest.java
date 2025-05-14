package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.repository.PathHistoryRepository;
import com.sullung2yo.seatcatcher.train.domain.TrainArrivalState;
import com.sullung2yo.seatcatcher.train.dto.TrainCarDTO;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import com.sullung2yo.seatcatcher.train.service.SeatEventService;
import com.sullung2yo.seatcatcher.train.service.TrainSeatGroupService;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
import com.sullung2yo.seatcatcher.user.domain.User;
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
    private SeatEventService mockSeatEventService;

    @InjectMocks
    private PathHistoryRealtimeUpdateServiceImpl pathHistoryRealtimeUpdateServiceWithMock;



    ///=============================== 실시간 도착 정보를 통해 PathHistory 갱신 로직 테스트 =================================///
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
        verify(spyService, never()).useReleaseSeatService(any());
    }

    @Test
    @DisplayName("전에 열차가 발견됐었는데 이번엔 발견되지 않았을 때 TrainArrivalState 분기가 잘 이루어지는지 테스트")
    void processTrainState_whenTrainWasFoundBeforeButNowNotFound()
    {
        // isArrived 는 True 여야 하며, 하차 처리가 일어나야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).useReleaseSeatService(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_NOT_FOUND.getStateCode(),
                TrainArrivalState.STATE_ARRIVED,
                new PathHistory(),
                -1, 120, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).useReleaseSeatService(any());
    }

    @Test
    @DisplayName("열차가 ARRIVED 상태인 경우 TrainArrivalState 분기가 잘 이루어지는지 테스트")
    void processTrainState_whenTrainIsArrived()
    {
        // ARRIVED , ENTERING , DEPARTED 이 세 가지 경우에 대해서는 하차 처리가 잘 수행되어야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).useReleaseSeatService(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_ARRIVED.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                10, 30, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).useReleaseSeatService(any());
    }

    @Test
    @DisplayName("열차가 ENTERING 상태인 경우 TrainArrivalState 분기가 잘 이루어지는지 테스트2")
    void processTrainState_whenTrainIsEntering()
    {
        // ARRIVED , ENTERING , DEPARTED 이 세 가지 경우에 대해서는 하차 처리가 잘 수행되어야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).useReleaseSeatService(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_ENTERING.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                10, 30, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).useReleaseSeatService(any());
    }

    @Test
    @DisplayName("열차가 DEPARTED 상태인 경우 TrainArrivalState 분기가 잘 이루어지는지 테스트3")
    void processTrainState_whenTrainIsDeparted()
    {
        // ARRIVED , ENTERING , DEPARTED 이 세 가지 경우에 대해서는 하차 처리가 잘 수행되어야 함.
        //given
        PathHistoryRealtimeUpdateServiceImpl spyService = Mockito.spy(pathHistoryRealtimeUpdateServiceWithMock);
        doNothing().when(spyService).useReleaseSeatService(any());

        //when
        boolean isArrived = spyService.processTrainStateAndRefresh(
                TrainArrivalState.STATE_DEPARTED.getStateCode(),
                TrainArrivalState.STATE_DRIVING,
                new PathHistory(),
                10, 30, 40
        );

        //then
        assertThat(isArrived).isTrue();
        verify(spyService).useReleaseSeatService(any());
    }

    @Test
    @DisplayName("열차가 이전역 진입 중이며 오차가 적음에도 realtime 계산값보다 expected 계산값이 적다면 갱신되는지 테스트")
    void processTrainState_whenTrainIsEnteringButRealtimeValIsSmallerThanExpected()
    {
        //when
        when(mockPathHistoryRepository.save(any(PathHistory.class)))
                .thenReturn(new PathHistory());
        doNothing().when(mockPathHistoryEventService).publishPathHistoryEvent(nullable(Long.class));
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
        doNothing().when(mockPathHistoryEventService).publishPathHistoryEvent(nullable(Long.class));
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

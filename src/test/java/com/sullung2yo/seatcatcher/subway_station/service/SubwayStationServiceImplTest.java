package com.sullung2yo.seatcatcher.subway_station.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Slf4j
@ExtendWith(MockitoExtension.class)
class SubwayStationServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SubwayStationRepository subwayStationRepository;

    @InjectMocks // @Mock으로 Mocking된 객체를 자동으로 주입받는 SubwayStationServiceImpl 인스턴스 생성
    private SubwayStationServiceImpl subwayStationService; // 테스트 대상 인스턴스

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient); // webClientBuilder.build() 호출 시 Mocked webClient 반환하도록 설정
        subwayStationService = new SubwayStationServiceImpl(
                webClientBuilder,
                objectMapper,
                subwayStationRepository
        );
        ReflectionTestUtils.setField(subwayStationService, "liveApiKey", "testKey"); // 테스트 appleClientId 설정
    }

    @Test
    void testSaveSubwayData() {
        // Given
        SubwayStationData subwayStationData = new SubwayStationData();
        subwayStationData.setSubwayLine("1");

        // When
        subwayStationService.saveSubwayData(List.of(subwayStationData));

        // saveAll 메서드가 호출되었는지 확인
        verify(subwayStationRepository).saveAll(any(List.class));
    }

    @Test
    void testFindById() {
        // Given
        Long id = 1L;
        SubwayStation subwayStation = new SubwayStation();
        subwayStation.setId(id);

        // When
        when(subwayStationRepository.findById(id)).thenReturn(java.util.Optional.of(subwayStation));

        // Then
        SubwayStation foundStation = subwayStationService.findById(id);
        assertEquals(subwayStation.getId(), foundStation.getId());
    }

    @Test
    void testFindWithKeyword() {
        // Given
        SubwayStation subwayStation = new SubwayStation();
        String stationName = "Test station";
        subwayStation.setStationName(stationName);

        // When
        when(subwayStationRepository.findByStationNameContaining(stationName)).thenReturn(List.of(subwayStation));

        // Then
        List<SubwayStation> foundStations = subwayStationService.findWithKeyword(stationName);
        assertEquals(1, foundStations.size());
    }

//    @Test
//    void testFindWithAscendingOrder() {
//        // Given
//        SubwayStation subwayStation1 = new SubwayStation();
//        subwayStation1.setStationName("Test station");
//        subwayStation1.setAccumulateDistance(123.45f);
//        subwayStation1.setLine(Line.LINE_1);
//
//        SubwayStation subwayStation2 = new SubwayStation();
//        subwayStation2.setStationName("Another station");
//        subwayStation2.setAccumulateDistance(678.90f);
//        subwayStation2.setLine(Line.LINE_1);
//
//        // When
//        when(subwayStationRepository.findBy("station", Line.LINE_1, "up")).thenReturn(List.of(subwayStation1, subwayStation2)); // 정렬되지 않은 상태로 반환
//
//        // Then
//        List<SubwayStation> foundStations = subwayStationService.findWith("station", Line.LINE_1, "up");
//        assertFalse(foundStations.isEmpty());
//        assertEquals(2, subwayStationService.findWith("station", Line.LINE_1, "up").size());
//        assertTrue(foundStations.get(0).getAccumulateDistance() < foundStations.get(1).getAccumulateDistance());
//    }

//    @Test
//    void testFindWithDescendingOrder() {
//        // Given
//        SubwayStation subwayStation1 = new SubwayStation();
//        subwayStation1.setStationName("Test station");
//        subwayStation1.setAccumulateDistance(123.45f);
//        subwayStation1.setLine(Line.LINE_1);
//
//        SubwayStation subwayStation2 = new SubwayStation();
//        subwayStation2.setStationName("Another station");
//        subwayStation2.setAccumulateDistance(678.90f);
//        subwayStation2.setLine(Line.LINE_1);
//
//        // When
//        when(subwayStationRepository.findBy("station", Line.LINE_1, "down")).thenReturn(List.of(subwayStation1, subwayStation2)); // 정렬되지 않은 상태로 반환
//
//        // Then
//        // 서비스의 정렬 로직이 올바르게 동작하는지 확인
//        List<SubwayStation> foundStations = subwayStationService.findWith("station", Line.LINE_1, "down");
//        assertFalse(foundStations.isEmpty());
//        assertEquals(2, subwayStationService.findWith("station", Line.LINE_1, "down").size());
//        assertTrue(foundStations.get(0).getAccumulateDistance() > foundStations.get(1).getAccumulateDistance());
//    }

    @Test
    void findByStationNameAndLine() {
        // Given
        String stationName = "Test station";
        String lineNumber = "1";
        SubwayStation subwayStation = new SubwayStation();
        subwayStation.setStationName(stationName);
        subwayStation.setLine(Line.LINE_1);

        // When
        when(subwayStationRepository.findByStationNameAndLine(stationName, Line.LINE_1)).thenReturn(subwayStation);

        // Then
        SubwayStation foundStation = subwayStationService.findByStationNameAndLine(stationName, lineNumber);
        assertEquals(subwayStation.getId(), foundStation.getId());
    }

    @Test
    void fetchIncomingTrains_ShouldReturnResponseBody() {
        // given
        String lineNumber = "7";
        String departure = "상도";
        String fakeResponse = "{\"realtimeArrivalList\":[]}";

        // mock chain
        Mockito.when(webClient.get()).thenReturn(uriSpec);
        Mockito.when(uriSpec.uri(Mockito.anyString())).thenReturn(headersSpec);
        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(fakeResponse));

        // when
        Optional<String> result = subwayStationService.fetchIncomingTrains(lineNumber, departure);

        // then
        assertTrue(result.isPresent());
        assertEquals(fakeResponse, result.get());
    }

    @Test
    void parseIncomingDownDirectionResponse_ShouldReturnParsedList() throws JsonProcessingException {
        // 하행 방향 도착 정보 테스트
        // Given
        String lineNumber = "7";

        SubwayStation departure = new SubwayStation();
        departure.setStationName("상도");
        departure.setAccumulateDistance(987.65f);
        departure.setLine(Line.LINE_7);

        SubwayStation destination = new SubwayStation();
        destination.setStationName("숭실대입구");
        destination.setAccumulateDistance(678.90f);
        destination.setLine(Line.LINE_7);

        String fakeResponse = """
            {
              "errorMessage": { "code": "INFO-000" },
              "realtimeArrivalList": [
                {
                  "subwayId": "1007",
                  "bstatnNm": "7호선",
                  "barvlDt": "180",
                  "ordkey": "11002온수0"
                },
                {
                  "subwayId": "1007",
                  "bstatnNm": "7호선",
                  "barvlDt": "120",
                  "ordkey": "01002숭실대입구0"
                },
                {
                  "subwayId": "1007",
                  "subwayName": "7호선",
                  "arrivalTime": "300",
                  "ordkey": "12004석남0"
                }
              ]
            }
            """;

        JsonNode fakeTree = new ObjectMapper().readTree(fakeResponse);

        IncomingTrainsResponse res1 = new IncomingTrainsResponse();
        res1.setArrivalTime("180");
        res1.setArrivalTrainOrder("11002온수0");
        res1.setDestinationStationName("온수");

        IncomingTrainsResponse res2 = new IncomingTrainsResponse();
        res2.setArrivalTime("300");
        res2.setArrivalTrainOrder("12004석남0");
        res2.setDestinationStationName("석남");

        // Mock
        when(objectMapper.readTree(fakeResponse)).thenReturn(fakeTree);
        when(objectMapper.treeToValue(any(), eq(IncomingTrainsResponse.class)))
                .thenReturn(res1, res2); // 순차 반환

        // When
        List<IncomingTrainsResponse> result = subwayStationService.parseIncomingResponse(lineNumber, departure, destination, fakeResponse);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @AfterEach
    void tearDown() {
    }
}
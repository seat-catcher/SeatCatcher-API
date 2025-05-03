package com.sullung2yo.seatcatcher.subway_station.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SubwayException;
import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;


@Slf4j
@Service
public class SubwayStationServiceImpl implements SubwayStationService {

    @Value("${api.seoul.live.key}")
    private String liveApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final SubwayStationRepository subwayStationRepository;
    private static final Map<String, Long> accumulateTimeByLine = new HashMap<>(); // 노선 별 누적시간을 저장할 HashMap

    public SubwayStationServiceImpl(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            SubwayStationRepository subwayStationRepository
    ) {
        this.subwayStationRepository = subwayStationRepository;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveSubwayData(List<SubwayStationData> stations) {
        // Json에 들어있는 데이터 개수가 250개 언저리라 배치처리 안해도 될 듯 합니다.
        List<SubwayStation> subwayStations = new ArrayList<>();

        for (SubwayStationData station : stations) {
            SubwayStation subwayStation = SubwayStation.builder()
                    .stationName(station.getSubwayStationName())
                    .distance(station.getDistanceKm())
                    .timeMinSec(station.getHourMinutes())
                    .line(Line.findByName(station.getSubwayLine()))
                    .accumulateDistance(station.getAccumulatedDistance())
                    .build();

            // Line 정보 가져오기
            String lineName = station.getSubwayLine(); // 받은 데이터에 기록되어 있는 호선 정보를 받아오기
            Line subwayLine = Line.findByName(lineName);

            subwayStation.setLine(subwayLine);

            // 누적시간 계산
            calculateAccumulatedTime(subwayStation, station, lineName);
            subwayStations.add(subwayStation);
        }

        // 역 정보 저장 (bulk insert)
        subwayStationRepository.saveAll(subwayStations);

        log.info("성공적으로 지하철 역 정보를 초기화했습니다.");
    }

    @Override
    public SubwayStation findById(Long id) {
        return subwayStationRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<SubwayStation> findWithKeyword(String name) {
        return subwayStationRepository.findByStationNameContaining(name);
    }

    @Override
    public List<SubwayStation> findWith(String keyword, Line line, String order) {

        return subwayStationRepository.findBy(keyword, line, order);
    }

    @Override
    public SubwayStation findByStationNameAndLine(@NonNull String stationName, @NonNull String lineNumber) {
        Line subwayLine = Line.findByName(lineNumber);
        log.debug("[findByStationNameAndLine] : {}, {}", stationName, subwayLine);
        SubwayStation subwayStation = subwayStationRepository.findByStationNameAndLine(stationName, subwayLine);
        if (subwayStation == null) {
            log.error("[findByStationNameAndLine] 역 정보가 존재하지 않습니다.");
            throw new SubwayException("역 정보가 존재하지 않습니다.", ErrorCode.SUBWAY_NOT_FOUND);
        }
        return subwayStation;
    }

    @Override
    public Optional<String> fetchIncomingTrains(
            @NonNull String lineNumber,
            @NonNull String departureStation
    ) {
        String LIVE_TRAIN_LOCATION_API_URL = "http://swopenAPI.seoul.go.kr/api/subway/" + liveApiKey + "/json/realtimeStationArrival/0/5/" + departureStation;
        log.debug("실시간 열차 도착 정보 API 호출");
        log.debug("API PARAMS: {}, {}", lineNumber, departureStation);
        return Optional.ofNullable(webClient.get()
                .uri(LIVE_TRAIN_LOCATION_API_URL)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("API 호출 중 오류 발생", e))
                .doFinally(signal -> log.debug("실시간 열차 도착 정보 API 호출 완료"))
                .block()
        );
    }

    @Override
    public List<IncomingTrainsResponse> parseIncomingResponse(
            @NonNull String lineNumber,
            @NonNull SubwayStation departure,
            @NonNull SubwayStation destination,
            String response
    ) {
        if (response == null) {
            log.warn("API 응답이 null입니다.");
            return Collections.emptyList(); // API 응답이 null인 경우 빈 리스트 반환
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode errorMessage = rootNode.path("errorMessage");
            String code = errorMessage.path("code").asText();
            log.debug("API 응답 코드: {}", code);

            if (code.isEmpty() || (!code.equals("INFO-000") && !code.equals("INFO-200"))) {
                log.warn("API 응답 코드가 유효하지 않음: {}", code);
                return Collections.emptyList(); // 유효하지 않은 코드인 경우 빈 리스트 반환
            }

            JsonNode positionList = rootNode.path("realtimeArrivalList");
            List<IncomingTrainsResponse> responseList = new ArrayList<>();
            for (JsonNode node : positionList) {
                if (
                    node.path("subwayId").asText().equals(Line.convertForIncomingTrains(lineNumber)) &&
                    node.path("ordkey").asText().startsWith(calculateUpDown(departure, destination))
                ) { // 노선번호 필터링
                    responseList.add(objectMapper.treeToValue(node, IncomingTrainsResponse.class));
                }
            }
            log.debug("API 응답 개수: {}", responseList.size());
            return responseList;
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return Collections.emptyList(); // JSON 파싱 오류인 경우 빈 리스트 반환
        }
    }

    /**
     * 상행선과 하행선을 계산하는 메서드
     * getAccumulateDistance() 기준으로 start역의 누적거리가 end역의 누적거리보다 크면 상행선
     * getAccumulateDistance() 기준으로 start역의 누적거리가 end역의 누적거리보다 작으면 하행선
     * @param start 출발역 객체
     * @param end 도착역 객체
     * @return 상행선(0) 또는 하행선(1)
     */
    private String calculateUpDown(SubwayStation start, SubwayStation end) {
        if (start.getAccumulateDistance() - end.getAccumulateDistance() > 0) {
            return "0"; // 상행선
        } else {
            return "1"; // 하행선
        }
    }

    private void calculateAccumulatedTime(SubwayStation subwayStation, SubwayStationData subwayStationData, String lineName) {
        long stationSeconds = subwayStation.convertStringToSeconds(subwayStationData.getHourMinutes()); // 데이터소스 원본 값을 파싱해서 초로 변환
        long currentAccumulatedSeconds = accumulateTimeByLine.getOrDefault(lineName, 0L);
        long newAccumulated = currentAccumulatedSeconds + stationSeconds; // 현재 역까지의 누적시간을 계산
        accumulateTimeByLine.put(lineName, newAccumulated);
        subwayStation.setAccumulateTime(newAccumulated);
    }
}

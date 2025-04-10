package com.sullung2yo.seatcatcher.subway_station.service;


import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class SubwayStationService {
    private final SubwayStationRepository subwayStationRepository;
    private static final Map<String, Long> accumulateTimeByLine = new HashMap<>(); // 노선 별 누적시간을 저장할 HashMap

    @Transactional
    public void saveSubwayData(List<SubwayStationData> stations) {
        // Json에 들어있는 데이터 개수가 250개 언저리라 배치처리 안해도 될 듯 합니다.
        List<SubwayStation> subwayStations = new ArrayList<>();
        //List<SubwayStationSubwayLine> subwayStationSubwayLines = new ArrayList<>();

        for (SubwayStationData station : stations) {
            SubwayStation subwayStation = SubwayStation.builder()
                    .stationName(station.getSubwayStationName())
                    .distance(station.getDistanceKm())
                    .timeMinSec(station.getHourMinutes())
                    .accumulateDistance(station.getAccumulatedDistance())
                    .build();

            // Line 정보 가져오기
            String lineName = station.getSubwayLine(); // 받은 데이터에 기록되어 있는 호선 정보를 받아오기
            //SubwayLine subwayLine = subwayLineRepository.findByLineName(lineName);
            Line subwayLine = Line.findByName(lineName);

            //황유석 추가함
            subwayStation.setLine(subwayLine);

            /*
            if (subwayLine == null) {
                log.error("지하철 노선 정보를 찾을 수 없습니다. : " + lineName);
                throw new SubwayLineNotFoundException("지하철 노선 정보를 찾을 수 없습니다. : " + lineName, ErrorCode.SUBWAY_LINE_NOT_FOUND);
            }
            */ // 해당 예외처리는 Line 으로 이전했습니다.





            // 누적시간 계산
            calculateAccumulatedTime(subwayStation, station, lineName);
/*
            // Station와 Line 관계 설정 (중간 테이블 엔티티 생성)
            SubwayStationSubwayLine subwayStationSubwayLine = SubwayStationSubwayLine.builder()
                    .subwayStation(subwayStation)
                    .subwayLine(subwayLine)
                    .build();
            subwayStationSubwayLine.setRelationships(subwayStation, subwayLine); // 중간 테이블 관계 설정 -> SubwayStationSubwayLine.java 참고
*/
            // subwayStations, subwayStationSubwayLines 리스트에 추가
            subwayStations.add(subwayStation);
            //subwayStationSubwayLines.add(subwayStationSubwayLine);
        }

        // 역 정보 저장 (bulk insert)
        subwayStationRepository.saveAll(subwayStations);

        // 중간 테이블 정보 저장 (bulk insert)
        //subwayStationSubwayLineRepository.saveAll(subwayStationSubwayLines);

        log.info("성공적으로 지하철 역 정보를 초기화했습니다.");
    }

    private void calculateAccumulatedTime(SubwayStation subwayStation, SubwayStationData subwayStationData, String lineName) {
        long stationSeconds = subwayStation.convertStringToSeconds(subwayStationData.getHourMinutes()); // 데이터소스 원본 값을 파싱해서 초로 변환
        long currentAccumulatedSeconds = accumulateTimeByLine.getOrDefault(lineName, 0L);
        long newAccumulated = currentAccumulatedSeconds + stationSeconds; // 현재 역까지의 누적시간을 계산
        accumulateTimeByLine.put(lineName, newAccumulated);
        subwayStation.setAccumulateTime(newAccumulated);
    }
}

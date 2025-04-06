package com.sullung2yo.seatcatcher.subway_station.service;


import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.SubwayLineNotFoundException;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayLine;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStationSubwayLine;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayLineRepository;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationSubwayLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
@RequiredArgsConstructor
public class SubwayStationService {
    private final SubwayStationSubwayLineRepository subwayStationSubwayLineRepository;
    private final SubwayStationRepository subwayStationRepository;
    private final SubwayLineRepository subwayLineRepository;

    @Transactional
    public void saveSubwayData(List<SubwayStationData> stations) {
        // Json에 들어있는 데이터 개수가 250개 언저리라 배치처리 안해도 될 듯 합니다.
        long accumulateTime = 0L;
        List<SubwayStation> subwayStations = new ArrayList<>();
        List<SubwayStationSubwayLine> subwayStationSubwayLines = new ArrayList<>();
        for (SubwayStationData station : stations) {
            SubwayStation subwayStation = SubwayStation.builder()
                    .stationName(station.getSubwayStationName())
                    .distance(station.getDistanceKm())
                    .timeMinSec(station.getHourMinutes())
                    .accumulateDistance(station.getAccumulatedDistance())
                    .build();

            // 누적시간 계산
            long stationSeconds = subwayStation.convertStringToSeconds(station.getHourMinutes());
            accumulateTime += stationSeconds;
            subwayStation.setAccumulateTime(accumulateTime);

            // Line 정보 가져오기
            String lineName = station.getSubwayLine();
            log.debug("검색 노선 이름: " + lineName);
            SubwayLine subwayLine = subwayLineRepository.findByLineName(lineName);
            if (subwayLine == null) {
                log.error("지하철 노선 정보를 찾을 수 없습니다. : " + lineName);
                throw new SubwayLineNotFoundException("지하철 노선 정보를 찾을 수 없습니다. : " + lineName, ErrorCode.SUBWAY_LINE_NOT_FOUND);
            }

            // Station와 Line 관계 설정 (중간 테이블 엔티티 생성)
            SubwayStationSubwayLine subwayStationSubwayLine = SubwayStationSubwayLine.builder()
                    .subwayStation(subwayStation)
                    .subwayLine(subwayLine)
                    .build();
            subwayStationSubwayLine.setRelationships(subwayStation, subwayLine); // 중간 테이블 관계 설정 -> SubwayStationSubwayLine.java 참고

            // subwayStations, subwayStationSubwayLines 리스트에 추가
            subwayStations.add(subwayStation);
            subwayStationSubwayLines.add(subwayStationSubwayLine);
        }

        // 역 정보 저장 (bulk insert)
        subwayStationRepository.saveAll(subwayStations);

        // 중간 테이블 정보 저장 (bulk insert)
        subwayStationSubwayLineRepository.saveAll(subwayStationSubwayLines);
    }

}

package com.sullung2yo.seatcatcher.subway_station.service;


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


@Slf4j
@Service
@RequiredArgsConstructor
public class SubwayStationService {

    private final SubwayStationSubwayLineRepository subwayStationSubwayLineRepository;
    private final SubwayStationRepository subwayStationRepository;
    private final SubwayLineRepository subwayLineRepository;

    @Transactional
    public void saveSubwayData(List<SubwayStationData> stations) {
        // Bulk Insert, Update
        List<SubwayStation> subwayStations = new ArrayList<>();
        List<SubwayStationSubwayLine> subwayStationSubwayLines = new ArrayList<>();
        for (SubwayStationData station : stations) {
            SubwayStation subwayStation = SubwayStation.builder()
                    .stationName(station.getSubwayStationName())
                    .distance(station.getDistanceKm())
                    .timeMinSec(station.getHourMinutes())
                    .accumulateDistance(station.getAccumulatedDistance())
                    .accumulateTime("0:00") // TODO: 이 부분은 나중에 수정 예정
                    .build();

            // Line 정보 가져오기
            String lineName = station.getSubwayLine();
            log.debug("검색 노선 이름: " + lineName);
            SubwayLine subwayLine = subwayLineRepository.findByLineName(lineName);
            if (subwayLine == null) {
                log.error("지하철 노선 정보를 찾을 수 없습니다. : " + lineName);
                throw new RuntimeException("지하철 노선 정보를 찾을 수 없습니다. : " + lineName);
            }

            // Station와 Line 관계 설정 (중간 테이블 엔티티 생성)
            SubwayStationSubwayLine subwayStationSubwayLine = SubwayStationSubwayLine.builder()
                    .subwayLine(subwayLine)
                    .subwayStation(subwayStation)
                    .build();

            // subwayStations, subwayStationSubwayLines 리스트에 추가
            subwayStations.add(subwayStation);
            subwayStationSubwayLines.add(subwayStationSubwayLine);

            // Line과 SubwayLine에 각각 중간 엔티티 정보 삽입
            subwayStation.getSubwayStationSubwayLines().add(subwayStationSubwayLine);
            subwayLine.getSubwayStationSubwayLines().add(subwayStationSubwayLine);
        }

        // 역 정보 저장
        subwayStationRepository.saveAll(subwayStations);

        // 중간 테이블 정보 저장
        subwayStationSubwayLineRepository.saveAll(subwayStationSubwayLines);
    }

}

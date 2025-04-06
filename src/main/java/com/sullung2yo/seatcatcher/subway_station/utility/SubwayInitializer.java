package com.sullung2yo.seatcatcher.subway_station.utility;

import com.sullung2yo.seatcatcher.subway_station.domain.SubwayLine;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayLineRepository;
import com.sullung2yo.seatcatcher.subway_station.service.SubwayStationService;
import com.sullung2yo.seatcatcher.subway_station.utility.parser.StationDataParser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class SubwayInitializer implements CommandLineRunner {

    private final SubwayLineRepository subwayLineRepository;
    private final StationDataParser stationDataParser;
    private final SubwayStationService subwayStationService;

    @Override
    public void run(String... args) throws Exception {
        // 1.
        // 1 ~ 8호선 노선 정보를 SubwayLine 테이블에 실행 시 저장 또는 업데이트한다 (로컬, 개발 환경)
        // 이미 존재하면 건너뛰고, 없으면 새로 만든다.
        for (int lineNum = 1; lineNum <= 9; lineNum++) {
            String lineName = String.valueOf(lineNum);

            boolean exists = subwayLineRepository.existsByLineName(lineName);
            if (!exists) {
                SubwayLine newLine = SubwayLine.builder()
                        .lineName(lineName)
                        .build();
                subwayLineRepository.save(newLine);
            }
        }

        // 2. resources/json/seoul_subway_info.json 파싱 -> 역 정보 리스트 가져오기
        List<SubwayStationData> subwayStationDataList = stationDataParser.parseJsonData(
                "src/main/resources/json/seoul_subway_info.json");

        // 3. 각 노선에 해당하는 역을 테이블에 저장
        subwayStationService.saveSubwayData(subwayStationDataList);

    }
}

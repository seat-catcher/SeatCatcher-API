package com.sullung2yo.seatcatcher.domain.subway_station.utility;

import com.sullung2yo.seatcatcher.domain.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.domain.subway_station.service.SubwayStationService;
import com.sullung2yo.seatcatcher.domain.subway_station.utility.parser.StationDataParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class SubwayInitializer implements CommandLineRunner {

    private final StationDataParser stationDataParser;
    private final SubwayStationService subwayStationService;

    @Override
    public void run(String... args) throws Exception {
        String filePath = "src/main/resources/json/seoul_subway_info.json";
        try {
            // resources/json/seoul_subway_info.json 불러와서 파싱 -> Station 정보 리스트 가져오기
            log.info("지하철 역 정보 JSON 파일 파싱...");
            List<SubwayStationData> subwayStationDataList = stationDataParser.parseJsonData(filePath);
            log.info("지하철 역 정보 JSON 파일 파싱 완료");

            // 각 노선에 해당하는 역을 테이블에 저장
            log.info("지하철 역 정보 저장...");
            subwayStationService.saveSubwayData(subwayStationDataList);
            log.info("지하철 역 정보 저장 완료");

            log.info("지하철 역 정보 초기화 완료");
        } catch (Exception e) {
            log.error("지하철 역 정보 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}

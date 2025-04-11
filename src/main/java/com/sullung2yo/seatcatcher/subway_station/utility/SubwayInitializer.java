package com.sullung2yo.seatcatcher.subway_station.utility;

import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.subway_station.service.SubwayStationService;
import com.sullung2yo.seatcatcher.subway_station.utility.parser.StationDataParser;
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
        // TODO : 추후 이걸 Production에도 올릴 때 실행시킬지 말지 결정해야 함
        int numberOfLines = 8; // 서울 지하철 노선 수 (서울 공공데이터가 9호선은 제공 안함)
        String filePath = "src/main/resources/json/seoul_subway_info.json";

        try {

            /*


            log.info("지하철 역 정보 초기화...");
            // 1 ~ 8호선 노선 정보를 SubwayLine 테이블에 실행 시 저장 또는 업데이트한다 (로컬, 개발 환경)
            for (int lineNum = 1; lineNum <= numberOfLines; lineNum++) {
                String lineName = String.valueOf(lineNum);

                boolean exists = subwayLineRepository.existsByLineName(lineName);
                if (!exists) { // 이미 존재하면 건너뛰고, 없으면 새로 만든다.
                    SubwayLine newLine = SubwayLine.builder()
                            .lineName(lineName)
                            .build();
                    subwayLineRepository.save(newLine);
                }
            }
            log.info("지하철 노선 정보 초기화 완료");


             */

            // 2. resources/json/seoul_subway_info.json 불러와서 파싱 -> Station 정보 리스트 가져오기
            log.info("지하철 역 정보 JSON 파일 파싱...");
            List<SubwayStationData> subwayStationDataList = stationDataParser.parseJsonData(filePath);
            log.info("지하철 역 정보 JSON 파일 파싱 완료");

            // 3. 각 노선에 해당하는 역을 테이블에 저장
            log.info("지하철 역 정보 저장...");
            subwayStationService.saveSubwayData(subwayStationDataList);
            log.info("지하철 역 정보 저장 완료");

            log.info("지하철 역 정보 초기화 완료");
        } catch (Exception e) {
            log.error("지하철 역 정보 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}

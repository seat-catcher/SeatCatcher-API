package com.sullung2yo.seatcatcher.subway_station.utility.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayDataRoot;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class StationDataJsonParser implements StationDataParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<SubwayStationData> parseJsonData(String filePath) throws IOException {
        // JSON 파일 지정
        File jsonFile = new File(filePath);
        log.debug("JSON 파일 읽기 성공");

        // SubwayDataRoot 객체로 역 전체 정보 매핑하기
        SubwayDataRoot root = objectMapper.readValue(jsonFile, SubwayDataRoot.class);
        log.debug("ObjectMapper를 사용한 JSON 파일 파싱 성공");

        // DESCRIPTION 정보 확인
        if (root.getDescription() != null) {
            log.debug("DESCRIPTION: {}", root.getDescription());
        }

        // 실제 역 정보 리스트 가져오기
        List<SubwayStationData> stations = root.getData();
        log.debug("총 {}개의 역 데이터가 파싱되었습니다.", (stations != null ? stations.size() : 0));

        return stations;
    }
}
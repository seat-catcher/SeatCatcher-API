package com.sullung2yo.seatcatcher.domain.subway_station.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.domain.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.domain.subway_station.utility.parser.StationDataJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@ExtendWith(SpringExtension.class) // 이 테스트는 Spring 컨텍스트에 크게 의존하지 않고, 단위 테스트(Parser) 테스트이므로 이걸 사용하는게 더 효율적이라고 하네요
class StationDataJsonParserTest {
    private StationDataJsonParser parser;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String filePath = "src/test/resources/subway_parse_test.json";

    @BeforeEach
    void setUp() {
        parser = new StationDataJsonParser(objectMapper);
    }

    @Test
    void testParseExceptionNotOccurs() {
        // 예외 발생 여부만 간단히 확인
        assertDoesNotThrow(() -> parser.parseJsonData(filePath));
    }

    @Test
    void testParseWithNonExistentFile() {
        String nonExistentFilePath = "invalid/path/to/non_existent_file.json";
        assertThrows(Exception.class, () -> parser.parseJsonData(nonExistentFilePath));
    }

    @Test
    void testInvalidJsonFormat() {
        String invalidJsonFilePath = "src/test/resources/invalid_subway_info.json";
        assertThrows(Exception.class, () -> parser.parseJsonData(invalidJsonFilePath));
    }

    @Test
    void testParseSubwayInfoJson() throws IOException {
        List<SubwayStationData> stations = parser.parseJsonData(filePath);

        // stations가 null이 아니고, 비어있지 않아야 함
        assertNotNull(stations, "역 정보 리스트가 null이 아니어야 합니다.");
        assertFalse(stations.isEmpty(), "역 정보 리스트가 비어 있으면 안 됩니다.");

        // 첫 번째 역 검증 (테스트용)
        log.debug("역 정보 리스트의 첫 번째 역: " + stations.get(0).toString());
        SubwayStationData first = stations.get(0);
        assertNotEquals("", first.getSubwayStationName(), "역 이름이 비어있습니다");
        assertNotEquals("", first.getSubwayLine(), "노선 번호가 비어있습니다");
        assertNotEquals(0.0f, first.getDistanceKm(), "거리 정보가 반영되지 않았습니다");
        assertNotEquals("0:00", first.getHourMinutes(), "소요 시간 정보가 반영되지 않았습니다");
        assertNotEquals(0.0f, first.getAccumulatedDistance(), "누적 거리 정보가 반영되지 않았습니다");
    }
}
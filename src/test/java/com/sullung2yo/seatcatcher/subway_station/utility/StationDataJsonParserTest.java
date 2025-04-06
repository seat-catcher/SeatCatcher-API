package com.sullung2yo.seatcatcher.subway_station.utility;

import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.subway_station.utility.parser.StationDataJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class StationDataJsonParserTest {

    private StationDataJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new StationDataJsonParser();
    }

    @Test
    void testParseExceptionNotOccurs() {
        // 예외 발생 여부만 간단히 확인
        assertDoesNotThrow(() -> parser.parseJsonData());
    }

    @Test
    void testParseSubwayInfoJson() {
        try {
            List<SubwayStationData> stations = parser.parseJsonData();

            // stations가 null이 아니고, 비어있지 않아야 함
            assertNotNull(stations, "역 정보 리스트가 null이 아니어야 합니다.");
            assertFalse(stations.isEmpty(), "역 정보 리스트가 비어 있으면 안 됩니다.");

            // 첫 번째 역 검증 (테스트용)
            log.debug("역 정보 리스트의 첫 번째 역: " + stations.get(0).toString());
            SubwayStationData first = stations.get(0);
            assertNotNull(first.getSubwayStationName(), "파싱이 잘못되었습니다. Json 파일을 확인하거나, 파싱 소스코드를 다시 확인해주세요");
            assertEquals("서울역", first.getSubwayStationName());
            assertEquals("1", first.getSubwayLine());
            assertEquals(0.0, first.getDistanceKm());

        } catch (Exception e) {
            fail("파싱 중 Exception 발생: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
    }
}
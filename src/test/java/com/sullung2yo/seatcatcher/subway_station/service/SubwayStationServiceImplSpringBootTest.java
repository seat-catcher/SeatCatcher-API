package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SubwayStationServiceImplSpringBootTest {

    @Autowired
    private SubwayStationServiceImpl subwayStationService;

    @Test
    @DisplayName("getPreviousStation Service 가 실제로 전역을 잘 반환하는지 테스트.")
    void getPreviousStationTest()
    {
        //given
        SubwayStation departure = subwayStationService.findWith("강남구청", Line.LINE_7, null)
                .get(0);

        SubwayStation end = subwayStationService.findWith("고속터미널", Line.LINE_7, null)
                .get(0);

        //when
        SubwayStation previousStation = subwayStationService.getPreviousStation(departure, end);

        //then
        assertThat(departure).isNotNull();
        assertThat(end).isNotNull();
        assertThat(previousStation).isNotNull();
        assertEquals("반포", previousStation.getStationName());

        //when
        previousStation = subwayStationService.getPreviousStation(end, departure);

        //then
        assertThat(previousStation).isNotNull();
        assertEquals("학동", previousStation.getStationName());
    }
}

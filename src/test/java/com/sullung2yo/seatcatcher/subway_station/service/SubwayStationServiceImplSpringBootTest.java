package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.domain.subway_station.enums.Line;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.subway_station.service.SubwayStationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    void getPreviousStationTest(){
        //given
        List<SubwayStation> departureStations = subwayStationService.findWith("강남구청", Line.LINE_7, null);
        assertThat(departureStations.size()).isEqualTo(1);
        SubwayStation departure = departureStations.get(0);

        List<SubwayStation> endStations = subwayStationService.findWith("고속터미널", Line.LINE_7, null);
        assertThat(endStations.size()).isEqualTo(1);
        SubwayStation end = endStations.get(0);

        //when
        SubwayStation previousStation = subwayStationService.getPreviousStation(departure, end);

        //then
        assertThat(departure).isNotNull();
        assertThat(end).isNotNull();
        assertThat(previousStation).isNotNull();
        assertThat(previousStation.getStationName()).isEqualTo("반포");

        //when
        previousStation = subwayStationService.getPreviousStation(end, departure);

        //then
        assertThat(previousStation).isNotNull();
        assertThat(previousStation.getStationName()).isEqualTo("학동");
    }
}

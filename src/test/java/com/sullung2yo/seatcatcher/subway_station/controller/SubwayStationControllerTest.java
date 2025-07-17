package com.sullung2yo.seatcatcher.subway_station.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.domain.subway_station.enums.Line;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.domain.subway_station.service.SubwayStationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class  SubwayStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubwayStationService subwayStationService;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private SubwayStation subwayStation;

    @BeforeEach
    void setUp(){
        // 일단 테스트를 하려면 샘플 지하철 역이 필요함.
        subwayStation = SubwayStation.builder()
                .stationName("안녕하세역")
                .line(Line.LINE_2)
                .timeMinSec("0:0")
                .accumulateTime(100)
                .distance(10)
                .accumulateDistance(10)
                .build();

        subwayStationRepository.save(subwayStation);

        subwayStationRepository.save(
                SubwayStation.builder()
                        .stationName("잘있으세역")
                        .line(Line.LINE_2)
                        .timeMinSec("0:0")
                        .accumulateTime(100)
                        .distance(10)
                        .accumulateDistance(100)
                        .build()
        );

        subwayStationRepository.save(
                SubwayStation.builder()
                        .stationName("또오세역")
                        .line(Line.LINE_7)
                        .timeMinSec("0:0")
                        .accumulateTime(100)
                        .distance(10)
                        .accumulateDistance(1000)
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllStations() throws Exception {
        //Given

        //When
        mockMvc.perform(get("/stations")
                        .param("keyword", "안녕")
                        .param("line", "2")
                        .param("order", "down"))
        //Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(subwayStation.getStationName()));

        //When
        mockMvc.perform(get("/stations")
                .param("order", "down"))
        //Then
                .andExpect(status().isOk());

        //When
        mockMvc.perform(get("/stations")
                        .param("line", "LINE_2"))
        //Then
                .andExpect(status().isOk());

        //When
        mockMvc.perform(get("/stations")
                        .param("line", "sdfasfsdfaf"))
        //Then
                .andExpect(status().isBadRequest());

        //When
        mockMvc.perform(get("/stations")
                        .param("keyword", "sdfasfsdfaf"))
                //Then
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetStation() throws Exception {
        //Given

        //When
        mockMvc.perform(get("/stations/{stationId}", subwayStation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(subwayStation.getStationName()));

        //When
        mockMvc.perform(get("/stations/{stationId}", 1230124141244L))
                .andExpect(status().isNotFound());
    }
}

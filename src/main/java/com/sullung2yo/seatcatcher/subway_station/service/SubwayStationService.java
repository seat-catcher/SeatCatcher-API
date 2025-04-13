package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface SubwayStationService {
    void saveSubwayData(List<SubwayStationData> stations);
    SubwayStation findById(Long id);
    SubwayStation findByStationNameAndLine(String stationName, String lineNumber);
    List<SubwayStation> findWithKeyword(String name);
    List<SubwayStation> findWith(String keyword, Line line, String order);
    Optional<String> fetchIncomingTrains(String lineNumber, String departureStation);
    List<IncomingTrainsResponse> parseIncomingResponse(String lineNumber, SubwayStation departure, SubwayStation destination, String response);
}

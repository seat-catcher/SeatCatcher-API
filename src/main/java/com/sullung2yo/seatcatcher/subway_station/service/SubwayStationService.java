package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;

import java.util.List;
import java.util.Optional;

public interface SubwayStationService {
    void saveSubwayData(List<SubwayStationData> stations);
    SubwayStation findById(Long id);
    List<SubwayStation> findWithKeyword(String name);
    List<SubwayStation> findWith(String keyword, Line line, String order);
}

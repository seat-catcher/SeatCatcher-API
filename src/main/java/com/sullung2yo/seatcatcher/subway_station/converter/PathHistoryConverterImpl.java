package com.sullung2yo.seatcatcher.subway_station.converter;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.user.domain.User;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class PathHistoryConverterImpl implements PathHistoryConverter{

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM.dd");

    @Override
    public PathHistory toPathHistory(User user, SubwayStation startStation, SubwayStation endStation) {
        return PathHistory.builder()
                .user(user)
                .startStation(startStation)
                .endStation(endStation)
                .build();
    }

    @Override
    public PathHistoryResponse toResponse(PathHistory pathHistory) {
        return PathHistoryResponse.builder()
                .id(pathHistory.getId())
                .startStationId(pathHistory.getStartStation().getId())
                .startStationName(pathHistory.getStartStation().getStationName())
                .endStationId(pathHistory.getEndStation().getId())
                .endStationName(pathHistory.getEndStation().getStationName())
                .expectedArrivalTime(pathHistory.getExpectedArrivalTime())
                .createdDate(pathHistory.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }
}

package com.sullung2yo.seatcatcher.subway_station.converter;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class PathHistoryConverterImpl implements PathHistoryConverter{
    @Override
    public PathHistory toPathHistory(User user, SubwayStation startStation, SubwayStation endStation) {
        return PathHistory.builder()
                .user(user)
                .startStation(startStation)
                .endStation(endStation)
                .build();
    }
}

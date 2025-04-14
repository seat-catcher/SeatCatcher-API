package com.sullung2yo.seatcatcher.subway_station.converter;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.user.domain.User;

public interface PathHistoryConverter {
    PathHistory toPathHistory(User user, SubwayStation startStation, SubwayStation endStation);
    PathHistoryResponse toResponse(PathHistory pathHistory);
}

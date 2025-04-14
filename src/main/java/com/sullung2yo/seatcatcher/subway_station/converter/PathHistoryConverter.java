package com.sullung2yo.seatcatcher.subway_station.converter;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
<<<<<<< HEAD
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.user.domain.User;

import java.util.List;

public interface PathHistoryConverter {
    PathHistory toPathHistory(User user, SubwayStation startStation, SubwayStation endStation);
    PathHistoryResponse.PathHistoryInfoResponse toResponse(PathHistory pathHistory);

    PathHistoryResponse.PathHistoryList toResponseList(ScrollPaginationCollection<PathHistory> pathHistoriesCursor, List<PathHistoryResponse.PathHistoryInfoResponse> pathHistoryList);
=======
import com.sullung2yo.seatcatcher.user.domain.User;

public interface PathHistoryConverter {
    PathHistory toPathHistory(User user, SubwayStation startStation, SubwayStation endStation);

>>>>>>> e10e7d2 ([FEAT] pathHistory 생성 api 구현)
}

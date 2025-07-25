package com.sullung2yo.seatcatcher.domain.path_history.converter;

import com.sullung2yo.seatcatcher.domain.path_history.entity.PathHistory;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.path_history.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.common.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.domain.user.entity.User;

import java.util.List;

public interface PathHistoryConverter {
    PathHistory toPathHistory(User user, SubwayStation startStation, SubwayStation endStation);
    PathHistoryResponse.PathHistoryInfoResponse toResponse(PathHistory pathHistory);

    PathHistoryResponse.PathHistoryList toResponseList(ScrollPaginationCollection<PathHistory> pathHistoriesCursor, List<PathHistoryResponse.PathHistoryInfoResponse> pathHistoryList);
}

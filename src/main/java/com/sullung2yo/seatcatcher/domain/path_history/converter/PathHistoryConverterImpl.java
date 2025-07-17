package com.sullung2yo.seatcatcher.domain.path_history.converter;

import com.sullung2yo.seatcatcher.domain.path_history.entity.PathHistory;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.path_history.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.user.domain.User;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
    public PathHistoryResponse.PathHistoryInfoResponse toResponse(PathHistory pathHistory) {
        return PathHistoryResponse.PathHistoryInfoResponse.builder()
                .id(pathHistory.getId())
                .startStationId(pathHistory.getStartStation().getId())
                .startStationName(pathHistory.getStartStation().getStationName())
                .startline(pathHistory.getStartStation().getLine())
                .endStationId(pathHistory.getEndStation().getId())
                .endStationName(pathHistory.getEndStation().getStationName())
                .endline(pathHistory.getEndStation().getLine())
                .expectedArrivalTime(pathHistory.getExpectedArrivalTime())
                .createdDate(pathHistory.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }

    @Override
    public PathHistoryResponse.PathHistoryList toResponseList(ScrollPaginationCollection<PathHistory> pathHistoriesCursor, List<PathHistoryResponse.PathHistoryInfoResponse> pathHistoryList) {
        return PathHistoryResponse.PathHistoryList.builder()
                .pathHistoryInfoList(pathHistoryList)
                .nextCursor(pathHistoriesCursor.getNextCursor() != null ? pathHistoriesCursor.getNextCursor().getId():-1L)
                .isLast(pathHistoriesCursor.isLastScroll())
                .build();
    }
}
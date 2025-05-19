package com.sullung2yo.seatcatcher.subway_station.service;

import com.google.type.DateTime;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;

import java.time.LocalDateTime;

public interface PathHistoryEventService {

    void publishPathHistoryEvent(Long pathHistoryId);
    void handlePathHistoryEvent(PathHistoryResponse.PathHistoryInfoResponse response);
}

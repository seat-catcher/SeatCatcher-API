package com.sullung2yo.seatcatcher.subway_station.service;

import com.google.type.DateTime;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.dto.response.StartJourneyResponse;

import java.time.LocalDateTime;

public interface PathHistoryEventService {

    void publishPathHistoryEvent(Long pathHistoryId, LocalDateTime nextScheduleTime, boolean isArrived);
    void handlePathHistoryEvent(StartJourneyResponse response);
}

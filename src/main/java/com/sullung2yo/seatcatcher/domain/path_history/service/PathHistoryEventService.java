package com.sullung2yo.seatcatcher.domain.path_history.service;

import com.sullung2yo.seatcatcher.domain.path_history.dto.response.StartJourneyResponse;

import java.time.LocalDateTime;

public interface PathHistoryEventService {

    void publishPathHistoryEvent(Long pathHistoryId, LocalDateTime nextScheduleTime, boolean isArrived);
    void handlePathHistoryEvent(StartJourneyResponse response);
}

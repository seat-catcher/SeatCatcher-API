package com.sullung2yo.seatcatcher.domain.path_history.service;

import com.sullung2yo.seatcatcher.domain.path_history.entity.PathHistory;
import com.sullung2yo.seatcatcher.domain.train.enums.TrainArrivalState;

public interface PathHistoryRealtimeUpdateService {
    long getNextScheduleTime(long seconds);
    void updateArrivalTimeAndSchedule(PathHistory pathHistory, String trainCode, TrainArrivalState beforeState);
}

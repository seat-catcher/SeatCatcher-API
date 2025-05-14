package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.train.domain.TrainArrivalState;

public interface PathHistoryRealtimeUpdateService {
    long getNextScheduleTime(long seconds);
    void updateArrivalTimeAndSchedule(PathHistory pathHistory, String trainCode, TrainArrivalState beforeState);
}

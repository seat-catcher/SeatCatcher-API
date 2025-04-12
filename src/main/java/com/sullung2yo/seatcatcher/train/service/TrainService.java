package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.dto.response.LiveTrainLocationResponse;

import java.util.List;

public interface TrainService {
    List<LiveTrainLocationResponse> fetchLiveTrainLocation(String lineNumber);
    void saveLiveTrainLocation();
}

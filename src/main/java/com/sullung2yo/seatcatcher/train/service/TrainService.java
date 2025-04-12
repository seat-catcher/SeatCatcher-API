package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.dto.response.TrainLocationResponse;

public interface TrainService {
    TrainLocationResponse getLocationForLine(String lineNumber);
    TrainLocationResponse filterTrainByTrainNumber(TrainLocationResponse response, String trainNumber);
}

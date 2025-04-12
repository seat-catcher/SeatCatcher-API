package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.dto.response.TrainLocationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TrainServiceImpl implements TrainService{
    @Override
    public TrainLocationResponse getLocationForLine(String lineNumber) {
        return null;
    }

    @Override
    public TrainLocationResponse filterTrainByTrainNumber(TrainLocationResponse response, String trainNumber) {
        return null;
    }
}

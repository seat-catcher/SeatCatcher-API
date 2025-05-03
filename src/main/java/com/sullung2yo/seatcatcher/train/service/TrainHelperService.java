package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.Train;

import java.util.List;

public interface TrainHelperService {
    List<Train> createGroupsOf(String trainCode, String carCode);
    Train create(String trainCode, String carCode, SeatGroupType groupType);
}

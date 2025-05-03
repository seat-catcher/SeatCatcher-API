package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;

import java.util.List;

public interface TrainHelperService {
    List<TrainSeatGroup> createGroupsOf(String trainCode, String carCode);
    TrainSeatGroup createTrain(String trainCode, String carCode, SeatGroupType groupType);
}

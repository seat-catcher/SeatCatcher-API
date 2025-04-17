package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;

import java.util.List;

public interface TrainSeatGroupService {

    TrainSeatGroup create(String trainCode, String carCode, SeatGroupType groupType);

    List<TrainSeatGroup> findByTrainCodeAndCarCode(String trainCode, String carCode);

    List<TrainSeatGroup> createGroupsOf(String trainCode, String carCode);
}

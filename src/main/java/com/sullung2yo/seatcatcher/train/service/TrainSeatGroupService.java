package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.dto.TrainCarDTO;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.user.domain.User;

import java.util.List;

public interface TrainSeatGroupService {

    List<TrainSeatGroup> findAllByTrainCodeAndCarCode(String trainCode, String carCode);
    List<TrainSeatGroup> createGroupsOf(String trainCode, String carCode);
    List<SeatInfoResponse> createSeatInfoResponse(String trainCode, String carCode, List<TrainSeatGroup> trainSeatGroups);
    TrainSeatGroup createTrainSeatGroup(String trainCode, String carCode, SeatGroupType groupType);

    TrainCarDTO getSittingTrainCarInfo(User user);
}
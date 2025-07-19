package com.sullung2yo.seatcatcher.domain.train.service;

import com.sullung2yo.seatcatcher.domain.train.enums.SeatGroupType;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import com.sullung2yo.seatcatcher.domain.train.dto.TrainCarDTO;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.domain.user.domain.User;

import java.util.List;

public interface TrainSeatGroupService {

    List<TrainSeatGroup> findAllByTrainCodeAndCarCode(String trainCode, String carCode);
    List<TrainSeatGroup> createGroupsOf(String trainCode, String carCode);
    List<SeatInfoResponse> createSeatInfoResponse(String trainCode, String carCode, List<TrainSeatGroup> trainSeatGroups);
    TrainSeatGroup createTrainSeatGroup(String trainCode, String carCode, SeatGroupType groupType);

    TrainCarDTO getSittingTrainCarInfo(User user);
}
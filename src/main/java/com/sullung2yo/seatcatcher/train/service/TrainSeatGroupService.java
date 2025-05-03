package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;

import java.util.List;

public interface TrainSeatGroupService {

    /*
        기본적으로 객체만 생성해주는 인터페이스입니다. 영속성 책임은 이 인터페이스를 호출하는
        쪽에 달려 있습니다.
    */
    TrainSeatGroup create(String trainCode, String carCode, SeatGroupType groupType);

    List<TrainSeatGroup> findAllByTrainCode(String trainCode, String carCode);
    List<TrainSeatGroup> findOrCreateByTrainCode(String trainCode, String carCode);

    List<TrainSeatGroup> createGroupsOf(String trainCode, String carCode);
}

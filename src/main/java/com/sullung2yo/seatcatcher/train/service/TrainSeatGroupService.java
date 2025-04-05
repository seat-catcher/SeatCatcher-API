package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainCar;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;

import java.util.List;

public interface TrainSeatGroupService {

    /*
        TrainCar 클래스를 생성한 뒤 해당 인터페이스를 호출해서 Car 에 세팅될 SeatGroup을 리턴받아 활용하세요!
    */
    public TrainSeatGroup create(TrainCar car, SeatGroupType groupType);


}

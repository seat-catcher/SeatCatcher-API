package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainCar;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;

import java.util.List;

public interface TrainSeatGroupService {

    /*
        TrainCar 클래스를 생성한 뒤 해당 인터페이스를 호출하면 Car 에 맞게 세팅된 SeatGroup이 리턴됩니다.
        이를 car 의 trainSeatGroups 에 차례대로 add 한 뒤 save 하시면 됩니다!
    */
    public TrainSeatGroup create(TrainCar car, SeatGroupType groupType);

    public List<TrainSeatGroup> findAllByTrainCarId(Long carId);

    public TrainSeatGroup findBySeatGroupId(Long seatGroupId);

    public void update(TrainSeatGroup trainSeatGroup);

    public void delete(TrainSeatGroup trainSeatGroup);
}

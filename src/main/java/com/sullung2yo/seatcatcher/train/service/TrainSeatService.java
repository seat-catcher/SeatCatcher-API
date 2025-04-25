package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.dto.request.TrainSeatRequest;

import java.util.List;

public interface TrainSeatService {

    // create 를 자체적으로 만들면 곤란해질 듯. TrainSeatGroup 에서 정해주는 규칙에 맞게 알아서 생성되게 냅두자!

    public List<TrainSeat> findAllBySeatId(Long id);

    public TrainSeat findById(Long id);

    public void update(Long id, TrainSeatRequest seatInfo);
}

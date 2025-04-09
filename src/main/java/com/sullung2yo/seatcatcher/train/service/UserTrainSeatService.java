package com.sullung2yo.seatcatcher.train.service;


import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;

public interface UserTrainSeatService {

    public void create(Long userId, Long seatId);

    public UserTrainSeat findUserTrainSeatByUserId(Long id);
    public UserTrainSeat findUserTrainSeatBySeatId(Long id);

    public void delete(Long id);
}

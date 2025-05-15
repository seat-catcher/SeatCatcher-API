package com.sullung2yo.seatcatcher.train.service;


import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;

public interface UserTrainSeatService {

    UserTrainSeat reserveSeat(Long userId, Long seatId);

    UserTrainSeat findUserTrainSeatByUserId(Long id);

    UserTrainSeat findUserTrainSeatBySeatId(Long id);

    TrainSeatGroup releaseSeat(Long id);

    void yieldSeat(Long seatId, Long giverID, Long takerId);

    boolean isUserSitting(Long userId);
}

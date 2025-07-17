package com.sullung2yo.seatcatcher.domain.train.service;


import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import com.sullung2yo.seatcatcher.domain.train.entity.UserTrainSeat;

public interface UserTrainSeatService {

    UserTrainSeat reserveSeat(Long userId, Long seatId);

    UserTrainSeat findUserTrainSeatByUserId(Long id);

    UserTrainSeat findUserTrainSeatBySeatId(Long id);

    void updateSeatOwner(Long userId, Long seatId, Long creditAmount);

    TrainSeatGroup releaseSeat(Long id);

    void yieldSeat(Long seatId, Long giverID, Long takerId);

    boolean isUserSitting(Long userId);
}

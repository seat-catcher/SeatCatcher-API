package com.sullung2yo.seatcatcher.train.service;


import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;

public interface UserTrainSeatService {

    void reserveSeat(Long userId, Long seatId);

    UserTrainSeat findUserTrainSeatByUserId(Long id);
    UserTrainSeat findUserTrainSeatBySeatId(Long id);

    void releaseSeat(Long id);

    void yieldSeat(Long seatId, Long giverID, Long takerId);
}

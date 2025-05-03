package com.sullung2yo.seatcatcher.train.service;


import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;

public interface UserTrainSeatService {

    void reserveSeat(Long userId, Long seatId);

    UserTrainSeat findUserTrainSeatByUserId(Long id);

    UserTrainSeat findUserTrainSeatBySeatId(Long id);

    void releaseSeat(Long id);

    void yieldSeat(Long seatId, Long giverID, Long takerId);

}

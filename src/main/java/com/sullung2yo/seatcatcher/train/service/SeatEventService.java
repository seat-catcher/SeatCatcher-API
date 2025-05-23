package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;

import java.util.List;
import java.util.Optional;

public interface SeatEventService {

    void publishSeatEvent(String trainCode, String carCode);
    void handleSeatEvent(List<SeatInfoResponse> seatInfoResponses);
    void publishSeatYieldEvent(Long seatId, YieldRequestType requestType, Long requestUserId, Optional<Long> oppositeUserId, Optional<Long> credit);
}

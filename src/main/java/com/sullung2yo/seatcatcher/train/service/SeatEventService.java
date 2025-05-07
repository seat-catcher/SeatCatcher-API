package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;

import java.util.List;
import java.util.Optional;

public interface SeatEventService {

    void issueSeatEvent(String trainCode, String carCode);
    void handleSeatEvent(List<SeatInfoResponse> seatInfoResponses);
    void issueSeatYieldEvent(Long seatId, YieldRequestType requestType, Long requestUserId, Optional<Long> oppositeUserId);
}

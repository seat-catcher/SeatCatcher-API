package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;

import java.util.List;

public interface SeatEventService {

    void issueSeatEvent(String trainCode, String carCode);
    void handleSeatEvent(List<SeatInfoResponse> seatInfoResponses);
    void issueSeatYieldRequestEvent(Long seatId, Long requestUserId);
}

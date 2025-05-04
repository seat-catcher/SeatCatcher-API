package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;

import java.util.List;

public interface SeatEventService {

    public void issueSeatEvent(String trainCode, String carCode);
    public void handleSeatEvent(SeatInfoResponse seatInfoResponse);
    public void publishSeatEvent(List<SeatInfoResponse> seatInfoResponses);
}

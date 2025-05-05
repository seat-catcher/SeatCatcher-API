package com.sullung2yo.seatcatcher.train.service;

public interface FcmService {
    void sendSeatYieldRequestNotification(Long occupantId, String title, String message, Long seatId);
}

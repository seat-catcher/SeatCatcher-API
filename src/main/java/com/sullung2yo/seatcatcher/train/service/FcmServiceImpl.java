package com.sullung2yo.seatcatcher.train.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmServiceImpl implements FcmService{
    @Override
    public void sendSeatYieldRequestNotification(Long occupantId, String title, String message, Long seatId) {

    }
}

package com.sullung2yo.seatcatcher.domain.alarm.service;

import com.sullung2yo.seatcatcher.domain.alarm.dto.request.FcmRequest;

import java.io.IOException;

public interface FcmService {
    void sendMessageTo(FcmRequest.Notification request) throws IOException;
    void sendMessageTo(FcmRequest.NotificationAndData request) throws IOException;
    void saveToken(FcmRequest.Token requset);
}

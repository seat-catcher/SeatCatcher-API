package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.PushNotificationType;
import com.sullung2yo.seatcatcher.user.dto.response.UserAlarmResponse;

import java.util.List;

public interface UserAlarmService {

    UserAlarmResponse.UserAlarmScrollResponse getMyAlarms(String token, int size, Long cursor, PushNotificationType type, Boolean isRead);
    void deletAlarm(Long id);
}

package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.PushNotificationType;
import com.sullung2yo.seatcatcher.user.dto.response.UserAlarmResponse;

import java.util.List;

public interface UserAlarmService {

    UserAlarmResponse.UserAlarmScrollResponse getMyAlarms(String token, int size, Long cursor, PushNotificationType type, Boolean isRead);
    UserAlarmResponse.UserAlarmItem getAlarm(String token, Long id);
    void deletAlarm(String token, Long id);

    void sendArrivalHandledAlarm(String receiverToken); // 자동 하차 처리 알람
    void sendSeatRequestReceivedAlarm(String receiverToken, String nickname); // 좌석 요청 도착 알림
    void sendArrivedAtFrontAlarm(String receiverToken, String nickname); // 앞자리에 도달 알림
    void sendSeatRequestRejectedAlarm(String receiverToken, String nickname); // 좌석 요청 거절 알림
    void sendSeatRequestAcceptedAlarm(String receiverToken, String nickname, String stationName); // 좌석 요청 수락 알림
    void sendSeatExchangeSuccessAlarm(String receiverToken, String nickname, int creditAmount); // 자리 교환 성공 알림
}

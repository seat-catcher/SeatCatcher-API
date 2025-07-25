package com.sullung2yo.seatcatcher.domain.alarm.service;

import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatYieldAcceptRejectResponse;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatYieldCanceledResponse;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatYieldRequestResponse;
import com.sullung2yo.seatcatcher.domain.alarm.enums.PushNotificationType;
import com.sullung2yo.seatcatcher.domain.alarm.dto.response.UserAlarmResponse;

public interface UserAlarmService {

    UserAlarmResponse.UserAlarmScrollResponse getMyAlarms(String token, int size, Long cursor, PushNotificationType type, Boolean isRead);
    UserAlarmResponse.UserAlarmItem getAlarm(String token, Long id);
    void deletAlarm(String token, Long id);

    void sendHelloAlarm(String token);

    void sendArrivalHandledAlarm(String receiverToken); // 자동 하차 처리 알람
    void sendSeatRequestReceivedAlarm(String receiverToken, String nickname, long creditAmount, SeatYieldRequestResponse response); // 좌석 요청 도착 알림
    void sendSeatRequestRejectedAlarm(String receiverToken, SeatYieldAcceptRejectResponse response); // 좌석 요청 거절 알림
    void sendSeatRequestAcceptedAlarm(String receiverToken, String nickname, String stationName, SeatYieldAcceptRejectResponse response); // 좌석 요청 수락 알림
    void sendSeatExchangeSuccessAlarm(String receiverToken, String nickname, int creditAmount); // 자리 교환 성공 알림
    void sendSeatRequestCanceledAlarm(String receiverToken, String nickname, SeatYieldCanceledResponse response); // 좌석 요청 취소 알림
}

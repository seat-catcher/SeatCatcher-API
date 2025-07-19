package com.sullung2yo.seatcatcher.domain.alarm.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SubwayException;
import com.sullung2yo.seatcatcher.common.exception.UserAlarmException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.common.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatYieldAcceptRejectResponse;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatYieldCanceledResponse;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatYieldRequestResponse;
import com.sullung2yo.seatcatcher.domain.alarm.converter.UserAlarmConverter;
import com.sullung2yo.seatcatcher.domain.alarm.enums.PushNotificationType;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.alarm.entity.UserAlarm;
import com.sullung2yo.seatcatcher.domain.alarm.dto.request.FcmRequest;
import com.sullung2yo.seatcatcher.domain.alarm.dto.response.UserAlarmResponse;
import com.sullung2yo.seatcatcher.domain.alarm.repository.UserAlarmRepository;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class UserAlarmServiceImpl implements UserAlarmService {

    private final UserService userService;
    private final UserAlarmRepository userAlarmRepository;
    private final UserAlarmConverter userAlarmConverter;
    private final FcmService fcmService;
    private final UserRepository userRepository;

    @Override
    public UserAlarmResponse.UserAlarmScrollResponse getMyAlarms(String token, int size, Long cursor, PushNotificationType type, Boolean isRead) {
        User user = userService.getUserWithToken(token);

        PageRequest pageRequest = PageRequest.of(0, size + 1);
        List<UserAlarm> alarms = userAlarmRepository.findScrollByUserAndCursor(user, type, isRead, cursor, pageRequest);

        ScrollPaginationCollection<UserAlarm> userAlarmCursor = ScrollPaginationCollection.of(alarms, size);

        List<UserAlarmResponse.UserAlarmItem> userAlarmItems = userAlarmCursor.getCurrentScrollItems().stream()
                .map(userAlarmConverter::toResponse)
                .toList();

        UserAlarmResponse.UserAlarmScrollResponse response = userAlarmConverter.toResponseList(userAlarmCursor,userAlarmItems);
        return response;
    }

    @Override
    public UserAlarmResponse.UserAlarmItem getAlarm(String token, Long id) {
        User user = userService.getUserWithToken(token);

        UserAlarm userAlarm = userAlarmRepository.findById(id)
                .orElseThrow(()-> new UserAlarmException("알람을 찾을 수 없습니다. id:" + id, ErrorCode.ALARM_NOT_FOUND));

        if(!userAlarm.getUser().equals(user))
            throw new SubwayException("해당 알람에 접근할 권한이 없습니다.",ErrorCode.ALARM_FORBIDDEN);

        userAlarm.setRead(true);
        userAlarmRepository.save(userAlarm);

        UserAlarmResponse.UserAlarmItem response =  userAlarmConverter.toResponse(userAlarm);

        return response;
    }

    @Override
    public void deletAlarm(String token, Long id) {
        User user = userService.getUserWithToken(token);

        UserAlarm userAlarm = userAlarmRepository.findById(id)
                .orElseThrow(()-> new UserAlarmException("알람을 찾을 수 없습니다. id:" + id, ErrorCode.ALARM_NOT_FOUND));

        if(!userAlarm.getUser().equals(user))
            throw new SubwayException("해당 알람에 접근할 권한이 없습니다.",ErrorCode.ALARM_FORBIDDEN);

        userAlarmRepository.deleteById(id);
    }


    private void send(String receiverToken, PushNotificationType type, Object... args) {
        sendFcmMessage(receiverToken, null, type, args);
    }

    private void send(String receiverToken, Object responseDTO, PushNotificationType type, Object... args) {
        sendFcmMessage(receiverToken, responseDTO, type, args);
    }

    private void sendFcmMessage(String receiverToken, Object responseDTO, PushNotificationType type, Object... args){
        String title = type.generateTitle(args);
        String body = type.generateBody(args);

        try{
            if(responseDTO == null)
            {
                FcmRequest.Notification fcmRequest = FcmRequest.Notification.builder()
                        .targetToken(receiverToken).title(title).body(body)
                        .build();
                fcmService.sendMessageTo(fcmRequest);
            }
            else
            {
                FcmRequest.NotificationAndData fcmRequest = FcmRequest.NotificationAndData.builder()
                        .targetToken(receiverToken).title(title).body(body).data(responseDTO)
                        .build();
                fcmService.sendMessageTo(fcmRequest);
            }
        } catch (IOException e) {
            log.error("FCM 메시지 전송 실패 - receiverToken: {}, message: {}", receiverToken, body, e);
        }

        saveUserAlarm(receiverToken, title, body, type);
    }

    private void saveUserAlarm(String receiverToken, String title, String body, PushNotificationType type){
        User user = userRepository.findByFcmToken(receiverToken)
                .orElseThrow(()->new UserException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        UserAlarm userAlarm = UserAlarm.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(type)
                .isRead(false)
                .build();

        userAlarmRepository.save(userAlarm);
    }

    @Override
    public void sendHelloAlarm(String token)
    {
        User user = userService.getUserWithToken(token);
        String fcmToken = user.getFcmToken();

        if(user.getFcmToken() == null)
        {
            throw new UserAlarmException("유저의 FcmToken 이 없습니다!", ErrorCode.INVALID_PARAMETER);
        }

        send(fcmToken, PushNotificationType.HELLO);
    }

    // 자동 하차 처리 알람
    @Override
    public void sendArrivalHandledAlarm(String receiverToken) {
        send(receiverToken, PushNotificationType.ARRIVAL_HANDLED);
    }

    /** 좌석 요청 도착 알림
     * @param receiverToken : 좌석 점유자의 FCM 토큰
     * @param nickname : 좌석 양보를 요청한 사람의 닉네임
     * @param creditAmount : 좌석 양보를 요청한 사람이 제시한 크레딧
     */
    @Override
    public void sendSeatRequestReceivedAlarm(String receiverToken, String nickname, long creditAmount, SeatYieldRequestResponse response) {
        send(receiverToken, response, PushNotificationType.SEAT_REQUEST_RECEIVED, nickname, creditAmount);
    }

    // 좌석 요청 거절 알림
    @Override
    public void sendSeatRequestRejectedAlarm(String receiverToken, SeatYieldAcceptRejectResponse response) {
        send(receiverToken, response, PushNotificationType.SEAT_REQUEST_REJECTED);
    }

    /** 좌석 요청 수락 알림
     *
     * @param receiverToken : 좌석 양보 요청을 한 사람의 FCM 토큰
     * @param nickname : 좌석에 앉아있는 사람의 닉네임
     * @param stationName : 좌석에 앉아있는 사람의 목적지 역 이름
     */
    @Override
    public void sendSeatRequestAcceptedAlarm(String receiverToken, String nickname, String stationName, SeatYieldAcceptRejectResponse response) {
        send(receiverToken, response, PushNotificationType.SEAT_REQUEST_ACCEPTED, nickname, stationName);
    }

    // 자리 교환 성공 알림
    // TODO : 알람 제목, 인자 이름 명확하게 수정, 로직 수정 필요
    @Override
    public void sendSeatExchangeSuccessAlarm(String receiverToken, String nickname, int creditAmount) {
        send(receiverToken, PushNotificationType.SEAT_EXCHANGE_SUCCESS, creditAmount, nickname, creditAmount);
    }

    /**
     * 좌석 요청 취소 알림
     * @param receiverToken : 자리에 앉아있는 사람의 FCM 토큰
     * @param nickname : 좌석 양보를 요청한 사람의 닉네임
     */
    @Override
    public void sendSeatRequestCanceledAlarm(String receiverToken, String nickname, SeatYieldCanceledResponse response) {
        send(receiverToken, response, PushNotificationType.SEAT_REQUEST_CANCELED, nickname);
    }
}

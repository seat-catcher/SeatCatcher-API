package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SeatException;
import com.sullung2yo.seatcatcher.subway_station.service.PathHistoryService;
import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.train.dto.response.SeatYieldAcceptRejectResponse;
import com.sullung2yo.seatcatcher.train.dto.response.SeatYieldCanceledResponse;
import com.sullung2yo.seatcatcher.train.dto.response.SeatYieldRequestResponse;
import com.sullung2yo.seatcatcher.user.domain.CreditPolicy;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.service.CreditService;
import com.sullung2yo.seatcatcher.user.service.UserAlarmService;
import com.sullung2yo.seatcatcher.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatEventServiceImpl implements SeatEventService {

    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;
    private final UserTrainSeatService userTrainSeatService;
    private final UserAlarmService userAlarmService;
    private final PathHistoryService pathHistoryService;
    private final CreditService creditService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    private final SimpMessagingTemplate webSocketMessagingTemplate;

    private final TrainSeatGroupService trainSeatGroupService;

    /**
     * 좌석 관련 이벤트를 발행하는 메서드입니다.
     * 좌석 그룹 정보를 가져오고, 없으면 새로 생성합니다.
     * 그 후, 응답 구조를 생성해서 RabbitMQ한테 작업을 수행하라고 넘겨줍니다.
     *
     * @param trainCode 기차 코드
     * @param carCode 차량 코드
     */
    public void publishSeatEvent(String trainCode, String carCode) {
        // 좌석 그룹 정보 가져오기
        List<TrainSeatGroup> trainSeatGroups = trainSeatGroupService.findAllByTrainCodeAndCarCode(trainCode, carCode);
        if (trainSeatGroups.isEmpty()) {
            log.warn("해당 열차 코드 : " + trainCode + "와 차량 코드 : " + carCode + "로 생성된 좌석 그룹이 없습니다. 새로 생성합니다.");
            trainSeatGroups = trainSeatGroupService.createGroupsOf(trainCode, carCode);
        }

        // 응답 구조 생성
        List<SeatInfoResponse> responses = trainSeatGroupService.createSeatInfoResponse(trainCode, carCode, trainSeatGroups);

        // RabbutMQ Exchange한테 메세지 발행
        log.info("좌석 이벤트 발행 이벤트 발생 : TrainCode :: {}", responses.get(0).getTrainCode());
        String routingKey = "seat" + "." + responses.get(0).getTrainCode();

        // Exchange한테 routingKey를 사용해서 seatEvent 담아서 전달
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, responses);
            log.debug("RabbitMQ에 좌석 이벤트 발행 성공: {}, {}", exchangeName, routingKey);
        } catch (Exception e) {
            log.error("RabbitMQ에 좌석 이벤트 발행 실패: {}, {}, {}", exchangeName, routingKey, e.getMessage());
        }
    }

    /**
     * 좌석 이벤트가 RabbitMQ의 Queue에 전달되었을 때 이를 자동으로 감지해서 처리하는 메서드입니다.
     * /topic/seat.{trainCode}를 구독한 클라이언트 전부한테 메세지를 내려줍니다. (topic 방식)
     * @param seatInfoResponses 좌석 정보 응답 객체 리스트
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}") // RabbitMQ Queue에 메세지가 들어오면 이 메서드가 호출됩니다.
    public void handleSeatEvent(List<SeatInfoResponse> seatInfoResponses){
        if (seatInfoResponses == null || seatInfoResponses.isEmpty()) {
            log.warn("좌석 이벤트 처리 실패: 좌석 정보가 없습니다.");
            return;
        }
        log.info("좌석 이벤트 처리 시작: TrainCode :: {}", seatInfoResponses.get(0).getTrainCode());
        String topic = "/topic/seat" + "." + seatInfoResponses.get(0).getTrainCode();

        try {
            webSocketMessagingTemplate.convertAndSend(topic, seatInfoResponses); // webSocket으로 topic 구독한 사람들에게 broadcast
            log.debug("웹소켓 메세지 전송 성공 : {}", topic);
        } catch (Exception e) {
            log.error("{}에 대한 웹소켓 메세지 전송 실패 : {}", topic, e.getMessage());
        }
    }


    /**
     * 좌석 양보 요청을 처리하는 메서드입니다.
     * @param seatId : 대상 좌석 ID
     * @param requestType : 요청 타입 (양보 요청: request, 양보 수락: accept, 양보 거절: reject)
     * @param requestUserId : 양보 요청을 보낸 사용자 ID or 수락/거절 시 좌석을 점유하고 있는 사용자 ID
     * @param oppositeUserId : 양보 요청을 받은 사용자 ID or 수락/거절 시 양보 요청을 보낸 사용자 ID
     * @param creditAmount : 양보 요청을 보낸 사용자 측에서 제시하는 크레딧 수
     */
    @Override
    @Transactional
    public void publishSeatYieldEvent(
            Long seatId,
            YieldRequestType requestType,
            Long requestUserId,
            Optional<Long> oppositeUserId,
            Optional<Long> creditAmount)
    {
        switch (requestType) {
            case REQUEST -> {
                if(creditAmount.isPresent()) {
                    this.handleYieldRequest(seatId, requestUserId, creditAmount.get());
                } else {
                    throw new SeatException("양보 요청 시 상대방에게 제안할 크레딧 수가 포함되어야 합니다.", ErrorCode.INVALID_PARAMETER);
                }
            }
            case CANCEL -> {
                if(creditAmount.isPresent())
                {
                    this.handleCancelYieldRequest(seatId, requestUserId, creditAmount.get());
                } else {
                    throw new SeatException("양보 요청 취소 시 상대방이 제안했던 크레딧 수가 포함되어야 합니다.", ErrorCode.INVALID_PARAMETER);
                }
            }
            case ACCEPT -> {
                if (oppositeUserId.isPresent()) {
                    this.handleAcceptYieldRequest(seatId, requestUserId, oppositeUserId.get());
                } else {
                    throw new SeatException("양보 요청을 수락 시 상대방 사용자 ID를 전달해야 합니다.", ErrorCode.INVALID_PARAMETER);
                }
            }
            case REJECT -> {
                if (oppositeUserId.isPresent()) {
                    if(creditAmount.isPresent()) {
                        this.handleRejectYieldRequest(seatId, requestUserId, oppositeUserId.get(), creditAmount.get());
                    } else {
                        throw new SeatException("양보 요청 거부 시 상대방이 제안했던 크레딧 수가 포함되어야 합니다.", ErrorCode.INVALID_PARAMETER);
                    }
                } else {
                    throw new SeatException("양보 요청을 거절 시 상대방 사용자 ID를 전달해야 합니다.", ErrorCode.INVALID_PARAMETER);
                }
            }
            default -> throw new SeatException("올바르지 않은 Request Type 입니다. : " + requestType, ErrorCode.INVALID_PARAMETER);
        }
    }

    /**
     * 양보 요청 시 호출되는 메서드.
     * 양보를 요청한 사용자는 /topic/seat.{seatId}.requester.{userId} 구독 -> 프론트에서 수행
     * 좌석을 현재 점유하고 있는 사용자의 경우 /topic/seat.{seatId}.owner 구독 -> 좌석 점유 시 구독
     * 기기의 상태값에 따라서 웹소켓 메세지를 보내거나 FCM 푸시 알림을 보내야 함
     * @param seatId : 좌석 ID
     * @param requestUserId : 양보 요청을 보낸 사용자 ID
     */
    protected void handleYieldRequest(Long seatId, Long requestUserId, Long creditAmount) {

        UserTrainSeat seat = userTrainSeatService.findUserTrainSeatBySeatId(seatId); // 좌석을 점유하고 있는 사용자
        User requestUser = userService.getUserWithId(requestUserId);
        User owner = seat.getUser();

        // 양보 요청을 보낸 사용자의 크레딧 감소 (서비스 내부에서 검증)
        creditService.creditModification(requestUserId, /*CreditPolicy.CREDIT_FOR_SIT_INFO_PROVIDE.getCredit()*/creditAmount, false, YieldRequestType.REQUEST);

        if (owner.getDeviceStatus()) { // 만약 좌석 점유자가 현재 앱을 사용중이라면, WebSocket 메세지 전송
            // OOO님이 좌석 양보 요청을 하셨어요 -> 이 메세지는 좌석을 점유하고 있는 사용자가 볼 수 있어야 함
            SeatYieldRequestResponse seatYieldRequestResponse = SeatYieldRequestResponse.builder()
                    .requestUserId(requestUserId)
                    .requestUserNickname(requestUser.getName())
                    .requestUserProfileImageNum(requestUser.getProfileImageNum())
                    .requestUserTags(requestUser.getUserTag())
                    .creditAmount(creditAmount)
                    .build(); // 좌석 양보 요청에 대한 응답 객체 생성

            String routingKey = "seat" + "." + seatId + "." + "owner"; // 좌석 점유자가 구독한 경로에다 전달

            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, seatYieldRequestResponse);
                log.debug("RabbitMQ에 좌석 양보 요청 이벤트 발행 성공: {}, {}", exchangeName, routingKey);
            } catch (Exception e) {
                log.error("RabbitMQ에 좌석 양보 요청 이벤트 발행 실패: {}, {}, {}", exchangeName, routingKey, e.getMessage());
            }
        } else {
            // FCM 푸시 알림 전송
            userAlarmService.sendSeatRequestReceivedAlarm(owner.getFcmToken(), requestUser.getName(), creditAmount);
            log.debug("좌석 요청 푸시 알람 전송 성공");
        }
    }

    /**
     * 양보 요청을 수락 또는 거절했을 때 호출되는 메서드입니다.
     * @param seatId : 대상 좌석 ID
     * @param requestUserId : 양보 요청을 수락한 사용자(좌석 점유자) ID
     * @param oppositeUserId : 양보 요청을 보낸 사용자 ID
     */
    protected void handleAcceptYieldRequest(Long seatId, Long requestUserId, Long oppositeUserId) { // TODO :: 테스트코드 작성 필요
        // requestUserId가 seatId를 점유하고 있는지 검증
        UserTrainSeat userTrainSeat = userTrainSeatService.findUserTrainSeatBySeatId(seatId);
        User owner = userTrainSeat.getUser();

        if (!Objects.equals(owner.getId(), requestUserId)) {
            log.error("양보 수락 실패: 양보를 수락한 사람이 좌석을 점유하고 있지 않습니다.");
            throw new SeatException("양보 수락 실패: 양보를 수락한 사람이 좌석을 점유하고 있지 않습니다.", ErrorCode.YIELD_ACCEPT_FAILED);
        }

        // 검증이 되었으므로, 양보를 수락했다는 메세지 생성 후, 양보를 요청한 사람(oppositeUser)한테 전달
        // 양보 요청 수락 시 크레딧 증가 로직은, 클라이언트에서 "좌석 교환" API를 호출할 때 처리됨
        User oppositeUser = userService.getUserWithId(oppositeUserId);
        if (oppositeUser.getDeviceStatus()) { // 만약 현재 앱을 사용중이라면, WebSocket 메세지 전송
            // OOO님이 좌석 양보 요청을 수락하셨어요 -> 이 메세지는 양보 요청을 보낸 사용자가 볼 수 있어야 함
            SeatYieldAcceptRejectResponse response = SeatYieldAcceptRejectResponse.builder()
                    .ownerId(requestUserId) // owner ID
                    .ownerNickname(owner.getName())
                    .ownerProfileImageNum(owner.getProfileImageNum())
                    .isAccepted(true)
                    .build();

            String routingKey = "seat." + seatId + "." + "requester." + oppositeUserId;
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, response);
                log.debug("RabbitMQ에 좌석 양보 수락 이벤트 발행 성공: {}, {}", exchangeName, routingKey);
            } catch (Exception e) {
                log.error("RabbitMQ에 좌석 양보 수락 이벤트 발행 실패: {}, {}, {}", exchangeName, routingKey, e.getMessage());
            }
        } else {
            // 좌석 양보 요청자에게 FCM 푸시 알림 전송
            // 좌석에 앉아있는 사람의 목적지
            Optional<String> destination = pathHistoryService.getUserDestination(owner);
            /*
                ㄴ 이거 검토해보셔야 할 것 같습니다.
                    유저가 PathHistory 를 자기 소유로 많이 가지고 있을 텐데
                    해당 서비스의 구현부를 살펴보면 현재 경로의 목적지가 아닌, 과거 경로의 목적지가 집계될 가능성이 있을 것 같습니다.
            */
            if (destination.isEmpty()) {
                log.error("양보 수락 실패: 양보를 수락한 사람(좌석 점유자)의 목적지를 찾을 수 없습니다.");
                throw new SeatException("양보 수락 실패: 양보를 수락한 사람(좌석 점유자)의 목적지를 찾을 수 없습니다.", ErrorCode.YIELD_ACCEPT_FAILED);
            }
            // 양보를 요청한 사람의 FCM 토큰을 통해 좌석 점유자가 양보를 수락/거절했다는 푸시 알림 전송
            userAlarmService.sendSeatRequestAcceptedAlarm(oppositeUser.getFcmToken(), owner.getName(), destination.get());
            log.debug("수락 푸시 알람 전송 성공");
        }
    }


    /**
     * 양보 요청을 거절했을 때 호출되는 메서드입니다.
     * @param seatId : 대상 좌석 ID
     * @param requestUserId : 양보 요청을 수락한 사용자(좌석 점유자) ID
     * @param oppositeUserId : 양보 요청을 보낸 사용자 ID
     * @param creditAmount : 복구될 크레딧 수
     */
    protected void handleRejectYieldRequest(Long seatId, Long requestUserId, Long oppositeUserId, Long creditAmount) { // TODO :: 테스트코드 작성 필요
        // requestUserId가 seatId를 점유하고 있는지 검증
        UserTrainSeat userTrainSeat = userTrainSeatService.findUserTrainSeatBySeatId(seatId);
        User owner = userTrainSeat.getUser();

        if (!Objects.equals(owner.getId(), requestUserId)) {
            log.error("양보 거절 실패: 양보를 거절한 사람이 좌석을 점유하고 있지 않습니다.");
            throw new SeatException("양보 거절 실패: 양보를 거절한 사람이 좌석을 점유하고 있지 않습니다.", ErrorCode.YIELD_ACCEPT_FAILED);
        }

        // 검증이 되었으므로, 양보를 거절했다는 메세지 생성 후, 양보를 요청한 사람(oppositeUser)한테 전달
        User oppositeUser = userService.getUserWithId(oppositeUserId);
        // 한 편, 요청이 거절됐으므로 요청할 때 지불했던 크레딧은 복구되어야 함.
        creditService.creditModification(oppositeUserId, creditAmount, true, YieldRequestType.REJECT); // TODO :: MVP 단계에선 상관 없지만 실제 서비스를 하게 되면 악성 클라이언트에게 악용될 가능성 존재. 이 부분 고려할 것.

        if (oppositeUser.getDeviceStatus()) { // 만약 현재 앱을 사용중이라면, WebSocket 메세지 전송
            // OOO님이 좌석 양보 요청을 거절하셨어요 -> 이 메세지는 양보 요청을 보낸 사용자가 볼 수 있어야 함
            SeatYieldAcceptRejectResponse response = SeatYieldAcceptRejectResponse.builder()
                    .ownerId(requestUserId) // owner ID
                    .ownerNickname(owner.getName())
                    .ownerProfileImageNum(owner.getProfileImageNum())
                    .isAccepted(false) // 거절당했으므로 false.
                    .build();

            String routingKey = "seat." + seatId + "." + "requester." + oppositeUserId;
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, response);
                log.debug("RabbitMQ에 좌석 양보 거절 이벤트 발행 성공: {}, {}", exchangeName, routingKey);
            } catch (Exception e) {
                log.error("RabbitMQ에 좌석 양보 거절 이벤트 발행 실패: {}, {}, {}", exchangeName, routingKey, e.getMessage());
            }
        } else {
            // 좌석 양보 요청자에게 FCM 푸시 알림 전송
            userAlarmService.sendSeatRequestRejectedAlarm(oppositeUser.getFcmToken());
            log.debug("거절 푸시 알람 전송 성공");
        }
    }

    /**
     * 좌석 양보를 취소했을 때 호출되는 메서드 입니다.
     * 취소 요청 시 프론트엔드는 구독한 웹소켓 토픽을 구독 해제 해야 합니다.
     */
    private void handleCancelYieldRequest(Long seatId, Long requestUserId, Long creditAmount) { // TODO :: 테스트코드 작성 필요
        // 취소 요청했을 때 -> 좌석에 앉아있는 사용자에게 해당 사용자는 양보 요청을 취소했습니다. 라는 메세지를 보내야 함
        UserTrainSeat seat = userTrainSeatService.findUserTrainSeatBySeatId(seatId); // 좌석을 점유하고 있는 사용자
        User requestUser = userService.getUserWithId(requestUserId); // 양보 요청을 보낸 사용자
        User owner = seat.getUser();

        // 양보 요청을 보낸 사용자는 요청을 취소했으므로 소모했던 크레딧을 반환받아야 함.
        creditService.creditModification(requestUser.getId(), creditAmount, true, YieldRequestType.CANCEL); // TODO :: MVP 단계에선 상관 없지만 실제 서비스를 하게 되면 악성 클라이언트에게 악용될 가능성 존재. 이 부분 고려할 것.

        if (owner.getDeviceStatus()) { // 만약 현재 앱을 사용중이라면, WebSocket 메세지 전송
            // OOO님이 좌석 양보 요청을 취소하셨어요 -> 이 메세지는 좌석을 점유하고 있는 사용자가 볼 수 있어야 함
            // 기존에 좌석 양보 요청 메세지는 알아서 프론트에서 처리?
            // 구독 해제도 프론트에서 해제
            SeatYieldCanceledResponse seatYieldCanceledResponse = SeatYieldCanceledResponse.builder()
                    .requestUserId(requestUserId)
                    .requestUserNickname(requestUser.getName())
                    .requestUserProfileImageNum(requestUser.getProfileImageNum())
                    .requestUserTags(requestUser.getUserTag())
                    .build(); // 좌석 양보 요청 취소 응답 객체 생성

            String routingKey = "seat" + "." + seatId + "." + "owner"; // 좌석에 앉아있는 사용자의 routingKey로 취소 메세지 전달해야함
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, seatYieldCanceledResponse);
                log.debug("RabbitMQ에 좌석 양보 요청 취소 이벤트 발행 성공: {}, {}", exchangeName, routingKey);
            } catch (Exception e) {
                log.error("RabbitMQ에 좌석 양보 요청 취소 이벤트 발행 실패: {}, {}, {}", exchangeName, routingKey, e.getMessage());
            }
        } else {
            // FCM 푸시 알림 전송
            userAlarmService.sendSeatRequestCanceledAlarm(owner.getFcmToken(), requestUser.getName());
            log.debug("취소 푸시 알람 전송 성공");
        }
    }
}

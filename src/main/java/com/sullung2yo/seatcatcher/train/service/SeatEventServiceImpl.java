package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.train.dto.response.SeatYieldRequestResponse;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatEventServiceImpl implements SeatEventService {

    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    private final SimpMessagingTemplate webSocketMessagingTemplate;

    private final TrainSeatGroupService trainSeatGroupService;

    /**
     * 좌석 관련 이벤트를 발행하는 메서드입니다.
     * 좌석 그룹 정보를 가져오고, 없으면 새로 생성합니다.
     * 그 후, 응답 구조를 생성해서 RabbitMQ한테 넘겨줍니다.
     *
     * @param trainCode 기차 코드
     * @param carCode 차량 코드
     */
    public void issueSeatEvent(String trainCode, String carCode) {
        // 좌석 그룹 정보 가져오기
        List<TrainSeatGroup> trainSeatGroups = trainSeatGroupService.findAllByTrainCodeAndCarCode(trainCode, carCode);
        if (trainSeatGroups.isEmpty()) {
            log.warn("해당 열차 코드 : " + trainCode + "와 차량 코드 : " + carCode + "로 생성된 좌석 그룹이 없습니다. 새로 생성합니다.");
            trainSeatGroups = trainSeatGroupService.createGroupsOf(trainCode, carCode);
        }

        // 응답 구조 생성
        List<SeatInfoResponse> responses = trainSeatGroupService.createSeatInfoResponse(trainCode, carCode, trainSeatGroups);
        // RabbutMQ Exchange한테 메세지 발행
        log.info("좌석 이벤트 발행 이벤트 발생 : TrainCode :: {}, CarCode :: {}", responses.get(0).getTrainCode(), responses.get(0).getCarCode());
        String routingKey = "seat" + "." + responses.get(0).getTrainCode() + "." + responses.get(0).getCarCode();

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
     * /topic/seat.{trainCode}.{carCode}를 구독한 클라이언트 전부한테 메세지를 내려줍니다. (topic 방식)
     * @param seatInfoResponses 좌석 정보 응답 객체 리스트
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}") // RabbitMQ Queue에 메세지가 들어오면 이 메서드가 호출됩니다.
    public void handleSeatEvent(List<SeatInfoResponse> seatInfoResponses){
        if (seatInfoResponses == null || seatInfoResponses.isEmpty()) {
            log.warn("좌석 이벤트 처리 실패: 좌석 정보가 없습니다.");
            return;
        }
        log.info("좌석 이벤트 처리 시작: TrainCode :: {}, CarCode :: {}", seatInfoResponses.get(0).getTrainCode(), seatInfoResponses.get(0).getCarCode());
        String topic = "/topic/seat" + "." + seatInfoResponses.get(0).getTrainCode() + "." + seatInfoResponses.get(0).getCarCode();

        try {
            webSocketMessagingTemplate.convertAndSend(topic, seatInfoResponses); // webSocket으로 topic 구독한 사람들에게 broadcast
            log.debug("웹소켓 메세지 전송 성공 : {}", topic);
        } catch (Exception e) {
            log.error("{}에 대한 웹소켓 메세지 전송 실패 : {}", topic, e.getMessage());
        }
    }

    /**
     * 양보 요청 이벤트를 발행하는 메서드입니다.
     * @param seatId
     * @param requestUserId
     */
    @Override
    public void issueSeatYieldRequestEvent(Long seatId, Long requestUserId) {
        // 양보를 요청한 사용자는 /topic/seat.{seatId}.requester 구독
        // 좌석을 현재 점유하고 있는 사용자의 경우 /topic/seat.{seatId}.owner 구독

        // 기기의 상태값에 따라서 웹소켓 메세지를 보내거나 FCM 푸시 알림을 보내야 함
        User requestUser = userService.getUserWithId(requestUserId);
        if (requestUser.getDeviceStatus()) { // 만약 현재 앱을 사용중이라면, WebSocket 메세지 전송
            SeatYieldRequestResponse seatYieldRequestResponse = SeatYieldRequestResponse.builder()
                    .requestUserId(requestUser.getId())
                    .requestUserNickname(requestUser.getName())
                    .requestUserProfileImageNum(requestUser.getProfileImageNum())
                    .requestUserTags(requestUser.getUserTag())
                    .build(); // 좌석 양보 요청에 대한 응답 객체 생성

            // OOO님이 좌석 양보 요청을 하셨어요 -> 이 메세지는 좌석을 점유하고 있는 사용자가 볼 수 있어야 함
            String routingKey = "seat" + "." + seatId + "." + "owner"; // 양보 요청을 받은 사용자의 routingKey로 전달해야함

            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, seatYieldRequestResponse);
                log.debug("RabbitMQ에 좌석 양보 요청 이벤트 발행 성공: {}, {}", exchangeName, routingKey);
            } catch (Exception e) {
                log.error("RabbitMQ에 좌석 양보 요청 이벤트 발행 실패: {}, {}, {}", exchangeName, routingKey, e.getMessage());
            }
        } else {
            // FCM 푸시 알림 전송
            // TODO :: FCM 푸시 알림 전송 로직 추가 필요
            log.debug("FCM 푸시 알림 전송 로직 추가 필요");
        }
    }
}

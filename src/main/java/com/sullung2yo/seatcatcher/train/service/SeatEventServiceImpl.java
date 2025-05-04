package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
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
        SeatInfoResponse response = trainSeatGroupService.createSeatInfoResponse(trainCode, carCode, trainSeatGroups);
        publishSeatEvent(response);
    }

    /**
     * 좌석 이벤트가 RabbitMQ의 Queue에 전달되었을 때 이를 자동으로 감지해서 처리하는 메서드입니다.
     * /topic/seat.{trainCode}.{carCode}를 구독한 클라이언트 전부한테 메세지를 내려줍니다. (topic 방식)
     * @param seatInfoResponse 좌석 정보 응답 객체
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}") // RabbitMQ Queue에 메세지가 들어오면 이 메서드가 호출됩니다.
    public void handleSeatEvent(SeatInfoResponse seatInfoResponse){
        log.info("좌석 이벤트 처리 시작 : {}", seatInfoResponse.toString());
        String topic = "/topic/seat" + "." + seatInfoResponse.getTrainCode() + "." + seatInfoResponse.getCarCode();

        try {
            webSocketMessagingTemplate.convertAndSend(topic, seatInfoResponse); // webSocket으로 topic 구독한 사람들에게 broadcast
            log.debug("웹소켓 메세지 전송 성공 : {}", topic);
        } catch (Exception e) {
            log.error("{}에 대한 웹소켓 메세지 전송 실패 : {}", topic, e.getMessage());
        }
    }

    /**
     * RabbitMQ에 좌석 이벤트를 전달하는 메서드입니다.
     * @param seatInfoResponse 좌석 정보 응답 객체
     */
    public void publishSeatEvent(SeatInfoResponse seatInfoResponse) {
        // RabbutMQ Exchange한테 메세지 발행
        log.info("좌석 이벤트 발행: {}", seatInfoResponse.toString());
        String routingKey = "seat" + "." + seatInfoResponse.getTrainCode() + "." + seatInfoResponse.getCarCode();

        // Exchange한테 routingKey를 사용해서 seatEvent 담아서 전달
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, seatInfoResponse);
            log.debug("RabbitMQ에 좌석 이벤트 발행 성공: {}, {}", exchangeName, routingKey);
        } catch (Exception e) {
            log.error("RabbitMQ에 좌석 이벤트 발행 실패: {}, {}, {}", exchangeName, routingKey, e.getMessage());
        }
    }
}

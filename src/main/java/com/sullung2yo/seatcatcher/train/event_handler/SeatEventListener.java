package com.sullung2yo.seatcatcher.train.event_handler;


import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SeatEventListener {
    /**
     * 좌석 이벤트가 RabbitMQ가 관리하는 Queue에 들어왔을 때 처리해주는 클래스
     */

    private final SimpMessagingTemplate webSocketMessagingTemplate;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleSeatEvent(SeatInfoResponse seatInfoResponse){
        log.info("좌석 이벤트 발생 : {}", seatInfoResponse.toString());
        String topic = "/topic/seat" + "." + seatInfoResponse.getTrainCode() + "." + seatInfoResponse.getCarCode();

        try {
            webSocketMessagingTemplate.convertAndSend(topic, seatInfoResponse); // webSocket으로 topic 구독한 사람들에게 broadcast
            log.debug("웹소켓 메세지 전송 성공 : {}", topic);
        } catch (Exception e) {
            log.error("{}에 대한 웹소켓 메세지 전송 실패 : {}", topic, e.getMessage());
        }
    }
}

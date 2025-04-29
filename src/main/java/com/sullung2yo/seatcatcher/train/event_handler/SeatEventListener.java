package com.sullung2yo.seatcatcher.train.event_handler;


import com.sullung2yo.seatcatcher.train.dto.event.SeatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SeatEventListener {
    /**
     * 좌석 이벤트가 RabbitMQ가 관리하는 Queue에 들어왔을 때 처리해주는 클래스
     */

    @Value("${rabbitmq.binding.prefix}")
    private String bindingPrefix;

    private final SimpMessagingTemplate webSocketMessagingTemplate;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleSeatEvent(SeatEvent seatEvent){
        log.info("좌석 이벤트 발생 : {}", seatEvent.toString());
        String topic = "/topic/" + bindingPrefix + "." + seatEvent.getTrainCode() + "." + seatEvent.getCarCode();

        try {
            webSocketMessagingTemplate.convertAndSend(topic, seatEvent); // webSocket으로 topic 구독한 사람들에게 broadcast
            log.debug("웹소켓 메세지 전송 성공 : {}", topic);
        } catch (Exception e) {
            log.error("{}에 대한 웹소켓 메세지 전송 실패 : {}", topic, e.getMessage());
        }
    }
}

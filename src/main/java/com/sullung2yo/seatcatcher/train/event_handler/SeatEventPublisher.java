package com.sullung2yo.seatcatcher.train.event_handler;

import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SeatEventPublisher {
    /**
     * 좌석 이벤트 발생 시 이벤트를 발행하는 클래스
     */
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    public void publish(SeatInfoResponse seatInfoResponse) {
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

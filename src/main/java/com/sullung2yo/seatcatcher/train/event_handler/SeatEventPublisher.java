package com.sullung2yo.seatcatcher.train.event_handler;

import com.sullung2yo.seatcatcher.train.dto.event.SeatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatEventPublisher {
    /**
     * 좌석 이벤트 발생 시 이벤트를 발행하는 클래스
     */
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.binding.prefix}")
    private String bindingPrefix;

    public void publish(SeatEvent seatEvent) {
        // RabbutMQ Exchange한테 메세지 발행
        String routingKey = bindingPrefix + "." + seatEvent.getTrainCode() + "." + seatEvent.getCarCode();

        // Exchange한테 routingKey를 사용해서 seatEvent 담아서 전달
        rabbitTemplate.convertAndSend(exchangeName, routingKey, seatEvent);
    }
}

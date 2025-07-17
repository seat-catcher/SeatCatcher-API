package com.sullung2yo.seatcatcher.domain.path_history.service;

import com.sullung2yo.seatcatcher.domain.path_history.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.domain.path_history.dto.response.StartJourneyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathHistoryEventServiceImpl implements PathHistoryEventService {

    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate webSocketMessagingTemplate;
    private final PathHistoryService pathHistoryService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    /*
        경로 기록 관련 이벤트 발생 메서드.
        PathHistory 에 대한 업데이트, 특히 도착 예정 시간이 업데이트되었을 경우 호출.
        함수는 바뀐 도착 예정 시간을 통해 응답 구조를 생성하여 RabbitMQ에게 넘겨줌.
    */

    @Override
    public void publishPathHistoryEvent(Long pathHistoryId, LocalDateTime nextScheduleTime, boolean isArrived) {
        // 입력받은 파라미터의 유효성 검증
        PathHistoryResponse.PathHistoryInfoResponse infoResponse = pathHistoryService.getPathHistoryAfterAuthenticate(pathHistoryId);
        // 예외처리는 service 계층에서 모두 수행됨. 따라서 여기에서는 생략.

        StartJourneyResponse response = StartJourneyResponse.builder()
                .pathHistoryId(infoResponse.getId())
                .expectedArrivalTime(infoResponse.getExpectedArrivalTime())
                .nextScheduleTime(nextScheduleTime)
                .isArrived(isArrived)
                .build();

        log.info("경로 예상 도착 시간 갱신 이벤트 발생 : 경로 ID :: {}, 예상 도착 시간 :: {}, 다음 스케줄 시간 :: {}, 도착 여부 :: {}",
                response.getPathHistoryId(),
                response.getExpectedArrivalTime(),
                response.getNextScheduleTime(),
                response.isArrived()
        );
        String pathHistoryRoutingKey = "path-histories" + "." + response.getPathHistoryId();

        // Exchange한테 routingKey를 사용해서 pathHistoryEvent 담아서 전달
        try {
            rabbitTemplate.convertAndSend(exchangeName, pathHistoryRoutingKey, response);
            log.debug("RabbitMQ에 경로 예상 도착 시간 갱신 이벤트 발행 성공: {}, {}", exchangeName, pathHistoryRoutingKey);
        } catch (Exception e) {
            log.error("RabbitMQ에 경로 예상 도착 시간 갱신 이벤트 발행 실패: {}, {}, {}", exchangeName, pathHistoryRoutingKey, e.getMessage());
        }
    }

    /*
        경로 기록 관련 이벤트가 RabbitMQ의 Queue 에 전달되었을 때 이를 자동으로 감지, 처리.
        /topic/pathHistory.{id} 를 구독한 클라이언트 전부한테 메시지를 내려줌. ( topic 방식 )
    */
    @Override
    @RabbitListener(queues = "${rabbitmq.path.queue.name}")
    public void handlePathHistoryEvent(StartJourneyResponse response) {
        if(response == null)
        {
            log.warn("경로 예상 도착 시간 갱신 이벤트 처리 실패 : 갱신 경로 정보가 없습니다.");
            return;
        }
        log.info("경로 예상 도착 시간 갱신 이벤트 처리 시작 : id :: {} 예상 도착 시간 :: {}, 다음 스케줄 시간 :: {}, 도착 여부 :: {}",
                response.getPathHistoryId(),
                response.getExpectedArrivalTime(),
                response.getNextScheduleTime(),
                response.isArrived());
        String topic = "/topic/path-histories" + "." + response.getPathHistoryId();

        try
        {
            webSocketMessagingTemplate.convertAndSend(topic, response); // webSocket으로 topic 구독한 사람들에게 broadcast
            log.debug("웹소켓 메세지 전송 성공 : {}", topic);
        }
        catch (Exception e)
        {
            log.error("{}에 대한 웹소켓 메세지 전송 실패 : {}", topic, e.getMessage());
        }
    }
}

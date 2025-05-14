package com.sullung2yo.seatcatcher.subway_station.service;

import com.google.type.DateTime;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
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
    public void publishPathHistoryEvent(Long pathHistoryId) {
        // 입력받은 파라미터의 유효성 검증
        // TODO :: 원석님이 만드신 service 사용하는데, 로직 보니까 확실히 작동할지는 모르겠음. 테스트 필요.
        PathHistoryResponse.PathHistoryInfoResponse response = pathHistoryService.getPathHistory(pathHistoryId);
        // 예외처리는 service 계층에서 모두 수행됨. 따라서 여기에서는 생략.

        log.info("경로 예상 도착 시간 갱신 이벤트 발생 : 경로 ID :: {}", response.getId());
        String pathHistoryRoutingKey = "path-histories" + "." + response.getId();

        // Exchange한테 routingKey를 사용해서 pathHistoryEvent 담아서 전달
        try {
            rabbitTemplate.convertAndSend(exchangeName, pathHistoryRoutingKey, response);
            log.debug("RabbitMQ에 경로 예상 도착 시간 갱신 이벤트 발행 성공: {}, {}", exchangeName, pathHistoryRoutingKey);
        } catch (Exception e) {
            log.error("RabbitMQ에 경로 예상 도착 시간 갱신 이벤트 발행 실패: {}, {}, {}", exchangeName, pathHistoryRoutingKey, e.getMessage());
        }

        // 유저가 자리를 점유하고 있다면 현재 탑승 중인 열차의 TrainCode, CarCode 를 알아낸 뒤 SeatEvent 또한 Publish해야 함.

    }

    /*
        경로 기록 관련 이벤트가 RabbitMQ의 Queue 에 전달되었을 때 이를 자동으로 감지, 처리.
        /topic/pathHistory.{id} 를 구독한 클라이언트 전부한테 메시지를 내려줌. ( topic 방식 )
    */
    @Override
    @RabbitListener(queues = "${rabbitmq.path.queue.name}")
    public void handlePathHistoryEvent(PathHistoryResponse.PathHistoryInfoResponse response) {
        if(response == null)
        {
            log.warn("경로 예상 도착 시간 갱신 이벤트 처리 실패 : 갱신 경로 정보가 없습니다.");
            return;
        }
        log.info("경로 예상 도착 시간 갱신 이벤트 처리 시작 : id :: {}", response.getId());
        String topic = "/topic/path-histories" + "." + response.getId();

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

package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.train.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Slf4j
@Controller
@RequiredArgsConstructor
public class TrainMessaging {

    // private final TrainService trainService;
    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/train/location/{lineNumber}/{trainNumber}")
    public void handlePrivateLocationRequest(@DestinationVariable String lineNumber, @DestinationVariable String trainNumber, Message<?> message) {
        // 공공API 호출을 통해 {lineNumber}에 해당하는 모든 실시간 열차 위치 정보 조회
        // 근데 구독할떄마다 외부 API를 호출하면 너무 비효율적이어서 이거 나중에 캐시 처리해서 가져오는걸로 바꿔야 함
        // 일단, 지금은 데이터베이스에다가만 실시간 데이터 주기적으로 저장하고, 로직 전부 구현한 뒤에 캐시 처리할 예정입니다!!
        // 클라이언트에게 전송
        messagingTemplate.convertAndSend("/topic/train/location/" + lineNumber + "/" + trainNumber, "temp");
    }
}

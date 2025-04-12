package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.train.dto.response.TrainLocationResponse;
import com.sullung2yo.seatcatcher.train.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;


@Slf4j
@Controller
@RequiredArgsConstructor
public class TrainMessaging {

    private final TrainService trainService;
    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/train/location/{lineNumber}/{trainNumber}")
    @Scheduled(initialDelay = 5000, fixedDelay = 30000) // 5초 후에 최초 실행, 이후 30초마다 실행
    public void handlePrivateLocationRequest(@DestinationVariable String lineNumber, @DestinationVariable String trainNumber, Message<?> message) {
        // 공공API 호출을 통해 {lineNumber}에 해당하는 모든 실시간 열차 위치 정보 조회
        TrainLocationResponse response = trainService.getLocationForLine(lineNumber); // TODO: trainService 구현 필요

        // trainNumber에 해당하는 열차만 필터링
        response = trainService.filterTrainByTrainNumber(response, trainNumber);

        // 클라이언트에게 전송
        messagingTemplate.convertAndSend("/topic/train/location/" + lineNumber + "/" + trainNumber, response);
    }
}

package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.train.dto.request.SeatInfoRequest;
import com.sullung2yo.seatcatcher.train.service.SeatEventService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket연결 후 /app/seat/request로 들어오는 메세지를 처리하는 컨트롤러입니다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SeatRequestController {

    private final SeatEventService seatEventService;

    @MessageMapping("/seat/request") // 클라이언트가 좌석 정보 요청할 때 -> /app/seat/request << 이 경로로 WebSocket SEND
    public void handleSeatRequest(@NonNull @Payload SeatInfoRequest payload, SimpMessageHeaderAccessor accessor) {
        try {
            log.debug("WebSocket을 통한 좌석 정보 요청 : {}", accessor.getUser());

            String trainCode = payload.getTrainCode();
            log.debug("요청한 열차 trainCode : {}", trainCode);

            String carCode = payload.getCarCode();
            log.debug("요청한 차량 carCode : {}", carCode);

            seatEventService.issueSeatEvent(trainCode, carCode);
        } catch (Exception e) {
            log.error("Websocket 기반 좌석 정보 요청 처리 중 오류 발생 : {}", e.getMessage());
            throw new RuntimeException("Websocket 기반 좌석 정보 요청 처리 중 오류 발생", e);
        } finally {
            log.debug("Websocket 좌석 정보 요청 처리 완료");
        }
    }

}

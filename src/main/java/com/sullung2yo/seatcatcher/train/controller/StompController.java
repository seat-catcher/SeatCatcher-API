package com.sullung2yo.seatcatcher.train.controller;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.UserException;
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
public class StompController {

    private final SeatEventService seatEventService;

    /**
     * 클라이언트에서, 차량의 전체 좌석 정보를 요청할 때 호출되는 메서드 입니다.
     * WebSocket을 통해 서버와 연결한 뒤,
     * /app/seat/request 경로로 WebSocket SEND를 요청하면
     * 좌석 관련 이벤트가 발생해서 /topic/seat.{trainCode}.{carCode} 경로를 구독한 모든 클라이언트들에게
     * 좌석 정보를 전송합니다.
     * @param payload 좌석 정보 요청 DTO
     * @param accessor SimpMessageHeaderAccessor
     */
    @MessageMapping("/seat/request")
    public void handleSeatRequest(@NonNull @Payload SeatInfoRequest payload, SimpMessageHeaderAccessor accessor) {
        try {
            if (accessor.getUser() == null) {
                throw new UserException("WebSocket에 연결된 사용자 정보가 서버에 없습니다.", ErrorCode.USER_NOT_FOUND);
            }
            log.debug("WebSocket을 통한 좌석 정보 요청 : {}", accessor.getUser());

            String trainCode = payload.getTrainCode();
            log.debug("요청한 열차 trainCode : {}", trainCode);

            String carCode = payload.getCarCode();
            log.debug("요청한 차량 carCode : {}", carCode);

            seatEventService.publishSeatEvent(trainCode, carCode);
        } catch (UserException e) {
            log.error(e.getMessage());
            throw new UserException("WebSocket에 연결된 사용자 정보가 서버에 없습니다.", ErrorCode.USER_NOT_FOUND);
        }
    }

}

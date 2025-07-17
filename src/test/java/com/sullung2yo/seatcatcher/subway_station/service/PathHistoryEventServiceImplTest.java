package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.domain.path_history.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.domain.path_history.dto.response.StartJourneyResponse;
import com.sullung2yo.seatcatcher.domain.path_history.service.PathHistoryEventServiceImpl;
import com.sullung2yo.seatcatcher.domain.path_history.service.PathHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(MockitoExtension.class)
public class PathHistoryEventServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private PathHistoryService pathHistoryService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private PathHistoryEventServiceImpl pathHistoryEventService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Test
    @DisplayName("Test for verify that event is really send to RabbitMQ")
    void publishPathHistoryEvent_shouldSendMessageToRabbitMQ() {

        //given
        PathHistoryResponse.PathHistoryInfoResponse pathHistoryInfoResponse = new PathHistoryResponse.PathHistoryInfoResponse();
        pathHistoryInfoResponse.setId(1L);

        when(pathHistoryService.getPathHistoryAfterAuthenticate(1L)).thenReturn(pathHistoryInfoResponse);

        //when
        pathHistoryEventService.publishPathHistoryEvent(1L, null, false);

        //then
        verify(rabbitTemplate).convertAndSend(eq(exchangeName), eq("path-histories.1"), any(StartJourneyResponse.class));
    }

    @Test
    void handlePathHistoryEvent_shouldSendMessageViaWebSocket() {

        // given
        Long pathHistoryId = 1L;
        StartJourneyResponse mockResponse =
                new StartJourneyResponse();
        mockResponse.setPathHistoryId(pathHistoryId);

        // when
        pathHistoryEventService.handlePathHistoryEvent(mockResponse);

        // then
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/path-histories.1"), eq(mockResponse));
    }
}

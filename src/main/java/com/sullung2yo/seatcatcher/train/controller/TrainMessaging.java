package com.sullung2yo.seatcatcher.train.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Slf4j
@Controller
public class TrainMessaging {

    @MessageMapping("/hello")
    @SendTo("/topic/hello")
    public String handleMessage(String message) throws Exception {
        log.debug("Received message: {}", message);
        return message;
    }
}

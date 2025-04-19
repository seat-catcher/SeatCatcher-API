package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.user.dto.request.FcmRequest;
import com.sullung2yo.seatcatcher.user.service.FcmService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;



    @PostMapping("/token")
    public ResponseEntity<String> saveFcmToken(@RequestBody FcmRequest.Token request) {

        fcmService.saveToken(request);

        return ResponseEntity.ok("token을 저장하였습니다.");
    }

    @PostMapping("/pushMessage")
    public ResponseEntity<String> pushMessage(@RequestBody FcmRequest.Notification request)  throws IOException {
        fcmService.sendMessageTo(request);
        return ResponseEntity.ok("알람을 성공적으로 전송하였습니다.");
    }
}

package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.user.dto.request.FcmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class FcmController {

    @PostMapping("/pushMessage")
    public ResponseEntity<String> pushMessage(@RequestBody FcmRequest requestDTO) {
        System.out.println(requestDTO.getTargetToken() + " " +requestDTO.getTitle() + " " + requestDTO.getBody());

        return ResponseEntity.ok("");
    }
}

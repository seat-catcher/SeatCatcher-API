package com.sullung2yo.seatcatcher.domain.alarm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@Schema(description = "firebase에 요청하기  위한 DTO 입니다.")
public class FcmMessage {

    private boolean validateOnly; //validateOnly: 유효성 검사만 실행하려면 true로 설정 -> 보통 false
    private Message message;
    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private String token;
        private Notification notification;
    }
    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }
}
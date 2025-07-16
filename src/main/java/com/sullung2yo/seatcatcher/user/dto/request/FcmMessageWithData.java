package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
@Schema(description = "firebase에 요청하기  위한 DTO 입니다.")
public class FcmMessageWithData {

    private boolean validateOnly; //validateOnly: 유효성 검사만 실행하려면 true로 설정 -> 보통 false
    private Message message;
    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private String token;
        private Notification notification;
        private Map<String, String> data; // ObjectMapper 의 writeValueAsString 함수를 통해 객체를 JSON 으로 변환한 후 넣어야 합니다!
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
package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
@Schema(description = "firebase에 요청하기  위한 DTO 입니다.")
public class FcmMessage {

<<<<<<< HEAD
    private boolean validateOnly; //validateOnly: 유효성 검사만 실행하려면 true로 설정 -> 보통 false
=======
    private boolean validateOnly; //validateOnly: 메시지를 실제로 보내는 대신, 유효성 검사만 실행하려면 true로 설정 -> 보통 false
>>>>>>> 393c332 ([FEAT] FCM 의존성 service dto 생성)
    private Message message;
    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
<<<<<<< HEAD
        private String token;
        private Notification notification;
=======
        private Notification notification;
        private String token;
>>>>>>> 393c332 ([FEAT] FCM 의존성 service dto 생성)
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
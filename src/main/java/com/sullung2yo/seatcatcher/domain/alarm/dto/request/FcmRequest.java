package com.sullung2yo.seatcatcher.domain.alarm.dto.request;

import com.sullung2yo.seatcatcher.domain.alarm.enums.PushNotificationType;
import lombok.*;

public class FcmRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Notification {
        private String targetToken;
        private String title;
        private String body;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Token {
        private String token;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationAndData {
        private String targetToken;
        private String title;
        private String body;
        private PushNotificationType type;

        private Object data;
    }
}
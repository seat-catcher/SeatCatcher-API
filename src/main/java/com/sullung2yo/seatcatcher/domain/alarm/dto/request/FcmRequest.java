package com.sullung2yo.seatcatcher.domain.alarm.dto.request;

import lombok.*;

public class FcmRequest {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Notification{
        private String targetToken;
        private String title;
        private String body;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class Token{
        private String token;

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationAndData{
        private String targetToken;
        private String title;
        private String body;

        Object data;
    }
}

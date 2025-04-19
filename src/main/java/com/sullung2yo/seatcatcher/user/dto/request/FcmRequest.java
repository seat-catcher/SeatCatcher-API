package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class FcmRequest {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class Notification{
        private String targetToken;
        private String title;
        private String body;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class Token{
//        private Long id;
        private String token;

    }

}

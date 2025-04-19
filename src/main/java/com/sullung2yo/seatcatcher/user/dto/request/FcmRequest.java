package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FcmRequest {
    private String targetToken;
    private String title;
    private String body;
}

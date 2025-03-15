package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    private String provider_access_token;
}

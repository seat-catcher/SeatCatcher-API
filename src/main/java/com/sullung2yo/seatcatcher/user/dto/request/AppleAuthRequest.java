package com.sullung2yo.seatcatcher.user.dto.request;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleAuthRequest extends AuthReqeust{
    private String authCode;

    public AppleAuthRequest() {
        this.setProvider(Provider.APPLE);
    }
}

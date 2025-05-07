package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class UserDeviceStatusUpdateRequest {
    private Boolean isActive;
}

package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    @NonNull
    private Long reportUserId;

    @NonNull
    private Long reportedUserId;

    @NonNull
    private String reason;
}

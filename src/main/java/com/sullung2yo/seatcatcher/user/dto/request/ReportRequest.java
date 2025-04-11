package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class ReportRequest {
    @NonNull
    private Long reportUserId;

    @NonNull
    private Long reportedUserId;

    @NonNull
    private String reason;
}

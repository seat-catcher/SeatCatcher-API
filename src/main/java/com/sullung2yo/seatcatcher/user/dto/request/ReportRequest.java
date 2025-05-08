package com.sullung2yo.seatcatcher.user.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {
    private Long reportUserId;
    private Long reportedUserId;
    private String reason;
}

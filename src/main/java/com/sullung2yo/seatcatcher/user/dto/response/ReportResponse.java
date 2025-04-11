package com.sullung2yo.seatcatcher.user.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long reportUserId;
    private String reportUserName;
    private Long reportedUserId;
    private String reportedUserName;
    private String reason;
}

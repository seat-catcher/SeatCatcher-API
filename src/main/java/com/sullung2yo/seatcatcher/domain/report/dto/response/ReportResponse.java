package com.sullung2yo.seatcatcher.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "신고 응답 DTO")
public class ReportResponse {

    @Schema(description = "신고자 ID")
    private Long reportUserId;

    @Schema(description = "신고자 이름")
    private String reportUserName;

    @Schema(description = "피신고자 ID")
    private Long reportedUserId;

    @Schema(description = "피신고자 이름")
    private String reportedUserName;

    @Schema(description = "신고 사유")
    private String reason;
}

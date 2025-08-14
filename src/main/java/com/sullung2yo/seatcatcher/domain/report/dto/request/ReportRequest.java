package com.sullung2yo.seatcatcher.domain.report.dto.request;

import com.google.firebase.database.annotations.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "신고 요청 DTO")
public class ReportRequest {

    @Schema(description = "신고자 ID")
    private Long reportUserId;

    @Schema(description = "피신고자 ID")
    private Long reportedUserId;

    @Schema(description = "신고 사유")
    private String reason;
}
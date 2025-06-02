package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Schema(description = "크레딧 증감 요청 DTO")
public class CreditModificationRequest {

    @NotNull
    @Min(1)
    @Schema(description = "크레딧 증감 값", example = "1000")
    private Long amount;

    @NotNull
    @Schema(description = "대상 사용자 ID", example = "1")
    private Long targetUserId;
}

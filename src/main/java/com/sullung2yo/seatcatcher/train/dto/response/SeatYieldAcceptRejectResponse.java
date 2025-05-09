package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "좌석 양보 수락/거절 응답 객체", title = "SeatYieldAcceptRejectResponse")
public class SeatYieldAcceptRejectResponse {

    @NotNull
    @Schema(description = "좌석 점유자 User Id", example = "1")
    Long ownerId;

    @NotNull
    @Schema(description = "좌석 점유자 닉네임", example = "asdfasdf")
    String ownerNickname;

    @NotNull
    @Schema(description = "좌석 점유자 프로필 이미지 번호", example = "IMAGE_1")
    ProfileImageNum ownerProfileImageNum;

    @NotNull
    @Schema(description = "좌석 양보 수락 여부", example = "true")
    Boolean isAccepted;
}

package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@Schema(description = "좌석 양보 수락/거절 응답 객체", title = "SeatYieldAcceptRejectResponse")
public class SeatYieldAcceptRejectResponse {

    @Schema(description = "좌석 ID", example = "1")
    Long seatId;

    @NotNull
    @Schema(description = "좌석 점유자 User Id", example = "1")
    Long ownerId;

    @NotNull
    @Schema(description = "양보 요청을 보낸 사람의 user id", example = "1")
    Long oppositeUserId;

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

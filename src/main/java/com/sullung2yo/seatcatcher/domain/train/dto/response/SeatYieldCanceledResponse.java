package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "좌석 양보 요청 취소 응답 객체", title = "SeatYieldCanceledResponse")
public class SeatYieldCanceledResponse {

    @Schema(description = "좌석 ID", example = "1")
    private Long seatId;

    @NotNull
    @Schema(description = "좌석 양보 요청을 취소한 사람의 User Id", example = "1")
    private Long requestUserId;

    @NotNull
    @Schema(description = "좌석 양보 요청을 취소한 사람의 닉네임", example = "asdfasdf")
    private String requestUserNickname;

    @NotNull
    @Schema(description = "좌석 양보 요청을 취소한 사람의 프로필 이미지 번호", example = "IMAGE_1")
    private ProfileImageNum requestUserProfileImageNum;
}

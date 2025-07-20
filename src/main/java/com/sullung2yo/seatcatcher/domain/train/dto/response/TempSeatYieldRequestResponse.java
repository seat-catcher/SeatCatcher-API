package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@Schema(description = "좌석 양보 요청 응답 객체", title = "SeatYieldRequestResponse")
public class TempSeatYieldRequestResponse {

    @Schema(description = "좌석 Id", example = "1")
    Long seatId;

    @NotNull
    @Schema(description = "좌석 양보를 요청한 사람의 User Id", example = "1")
    Long requestUserId;

    @NotNull
    @Schema(description = "좌석 양보를 요청한 사람의 닉네임", example = "asdfasdf")
    String requestUserNickname;

    @NotNull
    @Schema(description = "좌석 점유자 프로필 이미지 번호", example = "IMAGE_1")
    ProfileImageNum requestUserProfileImageNum;

    @NotNull
    @Schema(description = "좌석 양보를 요청한 사람이 제안한 크레딧 수", example = "300")
    Long creditAmount;

}

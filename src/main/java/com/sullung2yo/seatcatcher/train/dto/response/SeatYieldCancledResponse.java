package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.UserTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@Schema(description = "좌석 양보 요청 취소 응답 객체", title = "SeatYieldCanceledResponse")
public class SeatYieldCancledResponse {

    @NotNull
    @Schema(description = "좌석 양보 요청을 취소한 사람의 User Id", example = "1")
    Long requestUserId;

    @NotNull
    @Schema(description = "좌석 양보 요청을 취소한 사람의 닉네임", example = "asdfasdf")
    String requestUserNickname;

    @NotNull
    @Schema(description = "좌석 양보 요청을 취소한 사람의 프로필 이미지 번호", example = "IMAGE_1")
    ProfileImageNum requestUserProfileImageNum;

    @NotNull
    Set<UserTag> requestUserTags;
}

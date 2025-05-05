package com.sullung2yo.seatcatcher.train.dto.response;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.UserTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@Schema(description = "좌석 양보 요청 응답 객체", title = "SeatYieldRequestResponse")
public class SeatYieldRequestResponse {

    @NotNull
    Long requestUserId;

    @NotNull
    String requestUserNickname;

    @NotNull
    ProfileImageNum requestUserProfileImageNum;

    @NotNull
    Set<UserTag> requestUserTags;
}

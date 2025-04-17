package com.sullung2yo.seatcatcher.user.dto.request;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@Schema(description = "사용자 정보 수정 요청 DTO")
public class UserInformationUpdateRequest {

    @Schema(description = "사용자 닉네임", example = "user_asdf")
    private String name;

    @Schema(description = "사용자 프로필 이미지 번호", example = "IMAGE_1, IMAGE_2, ..., IMAGE_6")
    private ProfileImageNum profileImageNum;

    @Schema(
            description = "사용자 태그 목록 (USERTAG_NULL, USERTAG_LONGDISTANCE, USERTAG_PREGNANT, USERTAG_LOWHEALTH, USERTAG_DISABLED, USERTAG_CARRIER)",
            example = "[\"USERTAG_NULL\",\"USERTAG_LONGDISTANCE\"]"
    )
    private List<UserTagType> tags;

    @Schema(description = "사용자 크레딧", example = "1000")
    @Min(value = 0, message = "크레딧은 0 이상이어야 합니다.")
    private Long credit;

    @Schema(description = "온보딩 진행 여부", example = "false, true")
    private Boolean hasOnBoarded; // 온보딩 진행 여부

}

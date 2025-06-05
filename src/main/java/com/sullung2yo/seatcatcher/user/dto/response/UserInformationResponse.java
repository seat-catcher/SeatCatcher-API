package com.sullung2yo.seatcatcher.user.dto.response;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Schema(description = "사용자 정보 응답 DTO")
public class UserInformationResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "user_asdf")
    private String name;

    @Schema(description = "사용자 프로필 이미지 번호", example = "IMAGE_1")
    private ProfileImageNum profileImageNum;

    @Schema(description = "크레딧", example = "1000")
    private Long credit;

    @Schema(description = "사용자 태그 목록")
    private List<UserTagType> tags;

    @Schema(description = "온보딩 진행 여부", example = "false, true")
    private Boolean hasOnBoarded; // 온보딩 진행 여부
}

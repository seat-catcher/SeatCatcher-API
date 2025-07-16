package com.sullung2yo.seatcatcher.user.dto.request;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
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

    @Schema(description = "온보딩 진행 여부", example = "true, false")
    private Boolean hasOnBoarded; // 온보딩 진행 여부

    @Schema(description = "디바이스 Foreground/Background 여부", example = "true, false")
    private Boolean isActive; // 디바이스 Foreground/Background 여부

    @Schema(description = "애플 authorization code", example = "authorization_code_example")
    private String appleAuthorizationCode; // 애플 authorization code (회원탈퇴 시 토큰이 이미 만료된 경우 필요함)
}

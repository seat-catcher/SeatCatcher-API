package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "카카오 인증 요청 DTO")
public class KakaoAuthRequest{

    @NotNull
    @Schema(description = "카카오 Auth server에서 제공받은 accessToken", example = "qwer.asdf.zxcv")
    private String accessToken;

    @NotNull
    @Schema(description = "FCM 토큰", example = "fcmToken")
    private String fcmToken;
}

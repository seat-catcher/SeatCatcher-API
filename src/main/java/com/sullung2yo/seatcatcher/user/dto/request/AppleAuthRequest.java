package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "애플 인증 요청 DTO")
public class AppleAuthRequest {

    @NotNull
    @Schema(description = "애플 Auth server에서 제공받은 identityToken", example = "qwer.asdf.zxcv")
    private String identityToken;

    @NotNull
    @Schema(description = "FCM 토큰", example = "fcmToken")
    private String fcmToken;
}

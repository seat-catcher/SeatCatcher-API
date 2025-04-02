package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "카카오 인증 요청 DTO")
public class KakaoAuthRequest{
    @Schema(description = "카카오 Auth server에서 제공받은 accessToken", example = "qwer.asdf.zxcv")
    private String accessToken;
}

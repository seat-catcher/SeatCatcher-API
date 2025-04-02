package com.sullung2yo.seatcatcher.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Schema(description = "토큰 갱신 요청 DTO")
public class TokenRefreshRequest {
    @Schema(description = "Refresh token", example="qwer.asdf.zxcv")
    private String refreshToken;
}

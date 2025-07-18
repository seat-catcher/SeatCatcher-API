package com.sullung2yo.seatcatcher.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Access token 및 Refresh token 응답 DTO")
public class TokenResponse {
    @Schema(description = "Access token", example = "qwer.asdf.zxcv")
    private String accessToken;

    @Schema(description = "Refresh token", example = "qwer.asdf.zxcv")
    private String refreshToken;
}

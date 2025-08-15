package com.sullung2yo.seatcatcher.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoUserDataResponse {
    @Schema(description = "Kakao user id", example = "1234567890")
    private String id;
}
package com.sullung2yo.seatcatcher.user.dto.response;

import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tag 응답 DTO")
public class TagResponse {

    @Schema(description= "태그 id", example = "1")
    private Long id;

    @Schema(description= "태그 이름", example = "tag1")
    private UserTagType tagName;
}

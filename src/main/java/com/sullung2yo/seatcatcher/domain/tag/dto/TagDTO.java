package com.sullung2yo.seatcatcher.domain.tag.dto;

import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import java.util.List;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagDTO {

    private List<UserTagType> tags;
}

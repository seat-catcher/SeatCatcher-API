package com.sullung2yo.seatcatcher.user.dto;

import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import lombok.Getter;

@Getter
public class TagDto {

    private final UserTagType tagName;

    public TagDto(UserTagType tagName) {
        this.tagName = tagName;
    }
}

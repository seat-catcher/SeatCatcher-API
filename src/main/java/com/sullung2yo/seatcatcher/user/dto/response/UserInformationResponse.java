package com.sullung2yo.seatcatcher.user.dto.response;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.UserTag;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserInformationResponse {

    private String name;

    private ProfileImageNum profileImageNum;

    private Long credit;

    private List<UserTagType> tags;
}

package com.sullung2yo.seatcatcher.user.dto.response;

import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserInformationResponse {

    private String name;

    private ProfileImageNum profileImageNum;

    private Long credit;

    private TagResponse tag;
}

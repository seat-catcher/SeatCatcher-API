package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatOccupant {
    private Long userId;
    private String nickname;
    private LocalDateTime expectedArrivalTime;
    private ProfileImageNum profileImageNum;
    private String getOffStationName;
    private List<UserTagType> tags;
}

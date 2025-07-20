package com.sullung2yo.seatcatcher.domain.train.dto.response;

import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class SeatOccupant {
    private Long userId; // 사용자 ID
    private String nickname; // 사용자 닉네임
    private LocalDateTime expectedArrivalTime; // 도착역까지 얼마나 남았는지
    private ProfileImageNum profileImageNum; // 프로필 이미지 번호
    private String getOffStationName; // 도착역 이름
    private List<UserTagType> tags; // 사용자 태그 목록
}

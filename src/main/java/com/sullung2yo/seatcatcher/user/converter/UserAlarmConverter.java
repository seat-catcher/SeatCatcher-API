package com.sullung2yo.seatcatcher.user.converter;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.user.domain.UserAlarm;
import com.sullung2yo.seatcatcher.user.dto.response.UserAlarmResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAlarmConverter {

    public UserAlarmResponse.UserAlarmItem toResponse(UserAlarm userAlarm){
        return UserAlarmResponse.UserAlarmItem.builder()
                .id(userAlarm.getId())
                .type(userAlarm.getType())
                .content(userAlarm.getContent())
                .isRead(userAlarm.isRead())
                .localDateTime(userAlarm.getCreatedAt())
                .build();
    }

    public UserAlarmResponse.UserAlarmScrollResponse toResponseList(ScrollPaginationCollection<UserAlarm> userAlarmCursor, List<UserAlarmResponse.UserAlarmItem> userAlarmItems) {
        return UserAlarmResponse.UserAlarmScrollResponse.builder()
                .userAlarmItemList(userAlarmItems)
                .nextCursor(userAlarmCursor.getNextCursor() != null ? userAlarmCursor.getNextCursor().getId():-1L)
                .isLast((userAlarmCursor.isLastScroll()))
                .build();
    }

}

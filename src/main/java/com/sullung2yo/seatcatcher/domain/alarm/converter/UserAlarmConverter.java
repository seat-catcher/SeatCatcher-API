package com.sullung2yo.seatcatcher.domain.alarm.converter;

import com.sullung2yo.seatcatcher.common.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.domain.alarm.entity.UserAlarm;
import com.sullung2yo.seatcatcher.domain.alarm.dto.response.UserAlarmResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAlarmConverter {

    public UserAlarmResponse.UserAlarmItem toResponse(UserAlarm userAlarm){
        return UserAlarmResponse.UserAlarmItem.builder()
                .id(userAlarm.getId())
                .type(userAlarm.getType())
                .title(userAlarm.getTitle())
                .body(userAlarm.getBody())
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

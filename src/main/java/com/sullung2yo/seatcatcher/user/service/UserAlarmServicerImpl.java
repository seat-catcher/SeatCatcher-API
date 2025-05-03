package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.user.converter.UserAlarmConverter;
import com.sullung2yo.seatcatcher.user.domain.PushNotificationType;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserAlarm;
import com.sullung2yo.seatcatcher.user.dto.response.UserAlarmResponse;
import com.sullung2yo.seatcatcher.user.repository.UserAlarmRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class UserAlarmServicerImpl implements UserAlarmService {
    private final UserService userService;
    private final UserAlarmRepository userAlarmRepository;
    private final UserAlarmConverter userAlarmConverter;

    @Override
    public UserAlarmResponse.UserAlarmScrollResponse getMyAlarms(String token, int size, Long cursor, PushNotificationType type, Boolean isRead) {
        User user = userService.getUserWithToken(token);

        PageRequest pageRequest = PageRequest.of(0, size + 1);
        List<UserAlarm> alarms = userAlarmRepository.findScrollByUserAndCursor(user, type, isRead, cursor, pageRequest);

        ScrollPaginationCollection<UserAlarm> userAlarmCursor = ScrollPaginationCollection.of(alarms, size);

        List<UserAlarmResponse.UserAlarmItem> userAlarmItems = userAlarmCursor.getCurrentScrollItems().stream()
                .map(userAlarmConverter::toResponse)
                .toList();

        UserAlarmResponse.UserAlarmScrollResponse response = userAlarmConverter.toResponseList(userAlarmCursor,userAlarmItems);
        return response;
    }

    @Override
    public void deletAlarm(Long id) {

    }
}

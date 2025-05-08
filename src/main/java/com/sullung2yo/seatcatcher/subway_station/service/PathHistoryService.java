package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;

import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.user.domain.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PathHistoryService {
    void addPathHistory(String token, PathHistoryRequest request);
    PathHistoryResponse.PathHistoryInfoResponse getPathHistory(Long pathId);
    PathHistoryResponse.PathHistoryList getAllPathHistory(int size, Long pathId);
    void deletePathHistory(Long pathId);

    Optional<String> getUserDestination(User user);

    // PathHistory 의 expectedArrivalTime 을 인자로 넣어, 현재 타임스탬프와 비교하여 남은 시간을 분 단위로 리턴해주는 서비스.
    long getRemainingMinutes(LocalDateTime expectedArrivalTime);


}

package com.sullung2yo.seatcatcher.domain.path_history.service;

import com.sullung2yo.seatcatcher.domain.path_history.entity.PathHistory;
import com.sullung2yo.seatcatcher.domain.path_history.dto.request.PathHistoryRequest;

import com.sullung2yo.seatcatcher.domain.path_history.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PathHistoryService {
    PathHistory addPathHistory(String token, PathHistoryRequest request);
    PathHistoryResponse.PathHistoryInfoResponse getPathHistory(Long pathId);

    PathHistoryResponse.PathHistoryInfoResponse getPathHistoryAfterAuthenticate(Long pathId); // 이미 권한 인증을 끝낸 뒤 호출할 수 있는 서비스.

    PathHistoryResponse.PathHistoryList getAllPathHistory(int size, Long pathId);
    void deletePathHistory(Long pathId);

    Optional<String> getUserDestination(User user);

    // PathHistory 의 expectedArrivalTime 을 인자로 넣어, 현재 타임스탬프와 비교하여 남은 시간을 분 단위로 리턴해주는 서비스.
    long getRemainingSeconds(LocalDateTime expectedArrivalTime);
    // 어떤 유저가 가장 최근에 사용한 PathHistory를 가져옵니다.
    PathHistory getUsersLatestPathHistory(long userId);
    // 다음 스케줄을 언제 할 것인지를 해당 함수를 통해 반환받음.
}

package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;

import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.train.domain.TrainArrivalState;
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
    long getRemainingSeconds(LocalDateTime expectedArrivalTime);
    // 어떤 유저가 가장 최근에 사용한 PathHistory를 가져옵니다.
    PathHistory getUsersLatestPathHistory(long userId);
    // 다음 스케줄을 언제 할 것인지를 해당 함수를 통해 반환받음.
    long getNextScheduleTime(long seconds);
    // PathHistory 의 expectedArrivalTime을 갱신. 남은 시간을 재계산한 뒤 1/5 하여 다시 스케줄링함.
    // 이 때 남은 시간이 5초 미만으로 남게 되면 처리해야 할 조건들을 처리하고 "하차처리" 를 스케줄링함.
    void updateArrivalTimeAndSchedule(PathHistory pathHistory, String trainCode, TrainArrivalState beforeState);
}

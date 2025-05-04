package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.PathHistoryException;
import com.sullung2yo.seatcatcher.common.exception.SubwayException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.subway_station.converter.PathHistoryConverter;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.repository.PathHistoryRepository;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.subway_station.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathHistoryServiceImpl implements PathHistoryService{

    private final UserRepository userRepository;
    private final PathHistoryRepository pathHistoryRepository;
    private final SubwayStationRepository subwayStationRepository;
    private final PathHistoryConverter pathHistoryConverter;

    @Override
    public void addPathHistory(PathHistoryRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. id : " + request.getUserId(), ErrorCode.USER_NOT_FOUND));

        SubwayStation startStation = subwayStationRepository.findById(request.getStartStationId())
                .orElseThrow(() -> new SubwayException("해당 id를 가진 역을 찾을 수 없습니다. : "+request.getStartStationId(),ErrorCode.SUBWAY_STATION_NOT_FOUND ));

        SubwayStation endStation = subwayStationRepository.findById(request.getEndStationId())
                .orElseThrow(() -> new SubwayException("해당 id를 가진 역을 찾을 수 없습니다. : "+request.getEndStationId(),ErrorCode.SUBWAY_STATION_NOT_FOUND ));


        PathHistory newPathHistory = pathHistoryConverter.toPathHistory(user, startStation, endStation);
        pathHistoryRepository.save(newPathHistory);

        newPathHistory.calculateExpectedArrivalTime(startStation,endStation);

        pathHistoryRepository.save(newPathHistory);
    }

    @Override
    public PathHistoryResponse.PathHistoryInfoResponse getPathHistory(Long pathId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String providerId = authentication.getName();

        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. providerId : " + providerId, ErrorCode.USER_NOT_FOUND));

        PathHistory pathHistory = pathHistoryRepository.findById(pathId)
                .orElseThrow(() -> new SubwayException("해당 id를 가진 경로 이력을 찾을 수 없습니다. : " + pathId, ErrorCode.SUBWAY_STATION_NOT_FOUND ));

        if(!pathHistory.getUser().equals(user))
            throw new SubwayException("해당 경로 이력에 접근할 권한이 없습니다.",ErrorCode.PATH_HISTORY_FORBIDDEN);

        PathHistoryResponse.PathHistoryInfoResponse response = pathHistoryConverter.toResponse(pathHistory);
        return response;
    }

    @Override
    public PathHistoryResponse.PathHistoryList getAllPathHistory(int size, Long lastPathId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String providerId = authentication.getName();
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. providerId : " + providerId, ErrorCode.USER_NOT_FOUND));
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<PathHistory> pathHistories = pathHistoryRepository.findScrollByUserAndCursor(user, lastPathId, pageRequest);
        ScrollPaginationCollection<PathHistory> pathHistoriesCursor = ScrollPaginationCollection.of(pathHistories, size);

        List<PathHistoryResponse.PathHistoryInfoResponse> pathHistoryList = pathHistoriesCursor.getCurrentScrollItems().stream()
                .map(pathHistoryConverter::toResponse)
                .toList();

        PathHistoryResponse.PathHistoryList response = pathHistoryConverter.toResponseList(pathHistoriesCursor,pathHistoryList);

        return response;

    }

    @Override
    public void deletPathHistory(Long pathId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String providerId = authentication.getName();
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. providerId : " + providerId, ErrorCode.USER_NOT_FOUND));

        PathHistory pathHistory = pathHistoryRepository.findById(pathId)
                .orElseThrow(() -> new SubwayException("해당 id를 가진 경로 이력을 찾을 수 없습니다. : "+pathId,ErrorCode.SUBWAY_STATION_NOT_FOUND ));

        if(!pathHistory.getUser().equals(user))
            throw new SubwayException("해당 경로 이력에 접근할 권한이 없습니다.",ErrorCode.PATH_HISTORY_FORBIDDEN);

        pathHistoryRepository.delete(pathHistory);
    }


    @Override
    public long getRemainingSeconds(LocalDateTime expectedArrivalTime) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, expectedArrivalTime).toSeconds();
    }

    @Override
    public PathHistory getUsersLatestPathHistory(long userId) {

        // 일단 진짜 해당 Id를 가진 User 가 존재하는지 Verify
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. id : " + userId, ErrorCode.USER_NOT_FOUND));

        // 해당 userId 를 통해 pathHistory 검색, 그 중 updatedAt 이 가장 최근인걸 가져옴.
        return pathHistoryRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new PathHistoryException("사용자가 어떤 PathHistory도 소유하고 있지 않습니다.", ErrorCode.PATH_HISTORY_NOT_FOUND));
    }

    @Override
    public void updateArrivalTimeAndSchedule(PathHistory pathHistory, String trainCode, long beforeRemainingTimeSec) {
        /*
            우선 pathHistory 의 도착역 정보를 이용하여 실시간 역 도착 정보 API를 호출해야 함.

            그 후 인자인 trainCode 와 같은 code 를 가진 Response 가 있는지 확인해야 함.

                있다면, 해당 Response 에서 예상 도착 시간 정보를 기반으로 expectedArrivalTime 을 갱신.
                그 후 Websocket 으로 seatEvent 를 발행하여 클라이언트들이 갱신된 정보를 확인할 수 있도록 함.

                없다면 갱신하지 않음.

            다음으로 또 다시 스케줄링을 수행해야 함.

                만약 갱신에 성공했다면 remainingTimeSec 을 getRemainingSeconds 인터페이스로 재계산해야 함.
                갱신에 실패했다면 주어진 beforeRemainingTimeSec 을 쓰면 됨.

            remainingTimeSec / 5 를 인자로 updatedArrivalTimeAndSchedule 인터페이스를 다시 스케줄링.

            이 때 만약 remainingTimeSec 가 10초 미만이라면 updatedArrivalTimeAndSchedule 인터페이스 대신
            "하차처리" 인터페이스를 10초 뒤에 스케줄링.
        */
    }
}
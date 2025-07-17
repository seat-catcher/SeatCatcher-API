package com.sullung2yo.seatcatcher.domain.path_history.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.PathHistoryException;
import com.sullung2yo.seatcatcher.common.exception.SubwayException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.domain.path_history.converter.PathHistoryConverter;
import com.sullung2yo.seatcatcher.domain.path_history.entity.PathHistory;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.path_history.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.domain.path_history.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.domain.path_history.repository.PathHistoryRepository;
import com.sullung2yo.seatcatcher.domain.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.subway_station.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.service.UserService;
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
    private final UserService userService;
    private final PathHistoryRepository pathHistoryRepository;
    private final SubwayStationRepository subwayStationRepository;
    private final PathHistoryConverter pathHistoryConverter;

    @Override
    public PathHistory addPathHistory(String token, PathHistoryRequest request) {
        User user = userService.getUserWithToken(token);

        SubwayStation startStation = subwayStationRepository.findById(request.getStartStationId())
                .orElseThrow(() -> new SubwayException("해당 id를 가진 역을 찾을 수 없습니다. : "+request.getStartStationId(),ErrorCode.SUBWAY_STATION_NOT_FOUND ));

        SubwayStation endStation = subwayStationRepository.findById(request.getEndStationId())
                .orElseThrow(() -> new SubwayException("해당 id를 가진 역을 찾을 수 없습니다. : "+request.getEndStationId(),ErrorCode.SUBWAY_STATION_NOT_FOUND ));



        PathHistory newPathHistory = pathHistoryConverter.toPathHistory(user, startStation, endStation);
        pathHistoryRepository.save(newPathHistory);

        newPathHistory.calculateExpectedArrivalTime(startStation,endStation);

        pathHistoryRepository.save(newPathHistory);

        return newPathHistory; // 필요해서 새로 추가했습니다! - 황유석
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

        return pathHistoryConverter.toResponse(pathHistory);
    }

    @Override
    public PathHistoryResponse.PathHistoryInfoResponse getPathHistoryAfterAuthenticate(Long pathId) {
        PathHistory pathHistory = pathHistoryRepository.findById(pathId)
                .orElseThrow(() -> new SubwayException("해당 id를 가진 경로 이력을 찾을 수 없습니다. : " + pathId, ErrorCode.SUBWAY_STATION_NOT_FOUND ));
        return pathHistoryConverter.toResponse(pathHistory);
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

        return pathHistoryConverter.toResponseList(pathHistoriesCursor,pathHistoryList);

    }

    @Override
    public void deletePathHistory(Long pathId) {
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
    public Optional<String> getUserDestination(User user) {
        try{
            SubwayStation destination = this.getUsersLatestPathHistory(user.getId()).getEndStation(); // 사용자의 가장 최신 PathHistory 의 도착역을 가져옴.
            return Optional.of(destination.getStationName());
        } catch ( RuntimeException e ) {
            return Optional.empty();
        }
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
}
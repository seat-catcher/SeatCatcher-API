package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
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
import com.sullung2yo.seatcatcher.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
    public void addPathHistory(String token, PathHistoryRequest request) {
        User user = userService.getUserWithToken(token);

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
    public long getRemainingMinutes(LocalDateTime expectedArrivalTime) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, expectedArrivalTime).toMinutes();
    }
}
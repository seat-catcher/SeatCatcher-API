package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.PathHistoryException;
import com.sullung2yo.seatcatcher.common.exception.SubwayException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.common.service.TaskScheduleService;
import com.sullung2yo.seatcatcher.subway_station.converter.PathHistoryConverter;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.repository.PathHistoryRepository;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.subway_station.utility.ScrollPaginationCollection;
import com.sullung2yo.seatcatcher.train.domain.TrainArrivalState;
import com.sullung2yo.seatcatcher.train.dto.response.IncomingTrainsResponse;
import com.sullung2yo.seatcatcher.train.service.UserTrainSeatService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathHistoryServiceImpl implements PathHistoryService{

    private final UserRepository userRepository;
    private final UserService userService;
    private final PathHistoryRepository pathHistoryRepository;
    private final SubwayStationRepository subwayStationRepository;
    private final PathHistoryConverter pathHistoryConverter;
    private final SubwayStationService subwayStationService;
    private final UserTrainSeatService userTrainSeatService;
    private final TaskScheduleService scheduleService;

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
        SubwayStation destination = pathHistoryRepository.findEndStationByUser(user);
        return Optional.of(destination.getStationName());
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
    public long getNextScheduleTime(long seconds) {
        return seconds / 5;
    }

    @Override
    public void updateArrivalTimeAndSchedule(PathHistory pathHistory, String trainCode, TrainArrivalState beforeState) {

        //TODO :: 환경변수화할 것
        long refreshThreshold = 40; // 40초 이상 차이가 나는 경우 갱신
        long scheduleThreshold = 5; // 다음 스케줄

        long realtimeRemainingSeconds = -1;
        long expectedRemainingTime = Duration.between(pathHistory.getExpectedArrivalTime(), LocalDateTime.now()).toSeconds();
        int currentStateCode = TrainArrivalState.STATE_NOT_FOUND.getStateCode();


        // 우선 pathHistory의 도착역 정보를 이용하여 실시간 역 도착정보 API 호출
        IncomingTrainsResponse myResponse = fetchIncomingTrainsResponseByPathHistory(pathHistory, trainCode);

        if(myResponse == null) {
            // 못 찾았다!
            //currentStateCode = TrainArrivalState.STATE_NOT_FOUND.getStateCode(); // << 이미 돼있음. 생략.
            //realtimeRemainingSeconds = -1; // << 이미 돼있음. 생략.
        }
        else
        {
            currentStateCode = myResponse.getArrivalCode(); // 현재 state 값으로 Response 에서 추출한 ArrivalCode를 저장.
            realtimeRemainingSeconds = getRealtimeRemainingSeconds(myResponse, pathHistory); // Response 에서 예상 도착 시간 추출.
        }

        // 이 시점에 도착했을 때, 내 열차가 조회됐다면 currentStateCode 와 realtimeRemainingSeconds에
        // 유효한 값이 들어 있음. 이를 통해 PathHistory를 갱신해준다던가같은 작업을 수행하면 됨.

        boolean isArrived = processTrainState(currentStateCode, beforeState, pathHistory, realtimeRemainingSeconds, expectedRemainingTime, refreshThreshold);

        // 이 시점에서 해줘야 하는 처리는 다 해줬음. 이제 스케줄링을 해줄 차례.
        TrainArrivalState currentState = TrainArrivalState.getState(currentStateCode);

        if(isArrived) return; // 만약 모두 끝난 경우 스케줄링을 따로 안 해줘도 됨.
        else
        {
            expectedRemainingTime = Duration.between(pathHistory.getExpectedArrivalTime(), LocalDateTime.now()).toSeconds();
            long nextScheduleTime = getNextScheduleTime(expectedRemainingTime);
            if(nextScheduleTime < scheduleThreshold) // 너무 심하게 작다!
            {
                scheduleService.runThisAfterSeconds(scheduleThreshold, ()->
                {
                    updateArrivalTimeAndSchedule(pathHistory, trainCode, currentState);
                });
            }
            else
            {
                scheduleService.runThisAtBeforeSeconds(pathHistory.getExpectedArrivalTime(), nextScheduleTime, ()->
                {
                    updateArrivalTimeAndSchedule(pathHistory, trainCode, currentState);
                });
            }
        }
    }

    public IncomingTrainsResponse fetchIncomingTrainsResponseByPathHistory(PathHistory pathHistory, String trainCode) {
        SubwayStation endStation = pathHistory.getEndStation();
        String json = subwayStationService.fetchIncomingTrains(endStation.getLine().getName(), endStation.getStationName())
                .orElse(null);

        if(json == null)
        {
            // 이 경우 무언가의 이유에 의해 API 호출이 실패했다고 치고, 다음엔 되겠지 하는 마음으로 그냥 다음 스케줄이나 하러 감.
            /*DONOTHING*/
            log.info("PathHistoryService 에서 실시간 열차 위치 API 호출 결과 어떤 열차 정보도 반환받지 못 했습니다.");
            return null;
        }
        else
        {
            // Response 가 제대로 들어 있다면 이를 response 객체로 파싱
            List<IncomingTrainsResponse> responses = subwayStationService.parseIncomingResponse(
                    endStation.getLine().getName(),
                    pathHistory.getStartStation(),
                    pathHistory.getEndStation(),
                    json);

            IncomingTrainsResponse myResponse = null;

            // for 문을 돌리면서 내가 찾고 있는 열차가 있나 확인합시다.
            for(IncomingTrainsResponse response : responses)
            {
                if(response.getSubwayId().equals(trainCode)) {
                    myResponse = response;
                    break;
                }
            }
            if(myResponse == null) {
                // 아직 내가 찾는 열차가 조회되지 않음! 즉 따로 업데이트하지 않고 다음 스케줄이나 하러 가자.
                return null;
            }
            else {
                // 내가 추적하고 있는 열차를 찾았음!
                return myResponse;
            }
        }
    }

    public boolean processTrainState(int currentStateCode, TrainArrivalState beforeState, PathHistory pathHistory, long realtimeRemainingSeconds, long expectedRemainingTime, long refreshThreshold)
    {
        boolean isArrived = false;
        // 만약 못 찾은 경우
        // 해줘야 하는 작업 : Nothing or 하차처리
        if(currentStateCode == TrainArrivalState.STATE_NOT_FOUND.getStateCode()) {

            if(beforeState == TrainArrivalState.STATE_NOT_FOUND)
            {
                // 전에도 못 찾았다. 즉 그냥 엄청나게 긴 거리를 여행하는 승객이라 아직 열차를 못 찾은거임.
                /*Do Nothing*/
                // 이 경우 expected = 기존 값, realtime = -1 이 됨.
            }
            else // beforeState 는 STATE_NOT_FOUND 가 아니라, 확실히 탐지되고 있었다!
            {
                // 이 경우 열차가 이미 하차역을 지나가서 승객이 하차를 마친 상태임. 따라서 하차처리를 해야 함.
                useReleaseSeatService(pathHistory);
                isArrived = true;
            }
        }
        else
        // 일단 열차를 찾긴 했다!
        // 해줘야 하는 작업 : Nothing or PathHistory 갱신 or 하차처리
        {
            // 도착했으면 "하차처리" 를 수행해주어야 한다!
            if(currentStateCode == TrainArrivalState.STATE_ENTERING.getStateCode()
                    || currentStateCode == TrainArrivalState.STATE_ARRIVED.getStateCode()
                    || currentStateCode == TrainArrivalState.STATE_DEPARTED.getStateCode()) {
                //하차처리
                useReleaseSeatService(pathHistory);
                isArrived = true;
            }
            else
            {
                boolean isShouldRefresh = false;
                // 전역 진입중일 때 expectedRemainingTime 이 적다면 그건 이상함! 전역에 도착도 안 했는데 작으면 보정해줘야 함.
                if(currentStateCode == TrainArrivalState.STATE_PRVSTTN_ENTERING.getStateCode()
                        && realtimeRemainingSeconds != -1
                        && realtimeRemainingSeconds > expectedRemainingTime) {
                    isShouldRefresh = true;
                }
                // 나머지에서는 오차가 1분 이상 차이가 나면 realtimeRemainingSeconds 로 교체해주는 작업을 수행.
                else if(Math.abs(expectedRemainingTime - realtimeRemainingSeconds) > refreshThreshold) {
                    isShouldRefresh = true;
                }

                if(isShouldRefresh)
                {
                    pathHistory.setExpectedArrivalTime(LocalDateTime.now().plusSeconds(realtimeRemainingSeconds));
                    pathHistoryRepository.save(pathHistory);
                }
            }
        }
        return isArrived;
    }

    public long getRealtimeRemainingSeconds(IncomingTrainsResponse response, PathHistory pathHistory) {
        // 조회가 됐다!
        long realtimeRemainingSeconds = -1L;
        int arrivalCode = response.getArrivalCode();

        if(TrainArrivalState.getState(arrivalCode) == null)
        {
            log.error("response 로부터 받은 arrivalCode " + arrivalCode + "를 식별할 수 없습니다.");
            return -1;
        }

        if (!response.getArrivalTime().equals("0")) // 만약 정상적인 값을 반환받는 것이 가능하다면
        {
            realtimeRemainingSeconds = Long.parseLong(response.getArrivalTime()); // 그냥 받으면 됨.
        }
        else
        // barvlDt 에서 제대로 된 값을 받지 못 했다! 정해진 정책에 따라 값을 어떻게든 계산해내야 함.
        {
            // 그나마 몇 분인지 제공되는 경우. 이 경우 arrivalMessage 에서 앞에서부터 숫자만 추출하면 됨.
            if (arrivalCode == TrainArrivalState.STATE_DRIVING.getStateCode())
            {
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(response.getArrivalMessage());

                if (matcher.find()) {
                    String numberStr = matcher.group();
                    realtimeRemainingSeconds = Long.parseLong(numberStr) * 60;
                } else {
                    //엥???? 내가 알던 세상이 무너졌다!!
                    log.error("Error parsing arrival message: " + response.getArrivalMessage());
                    throw new RuntimeException("Error parsing arrival message: " + response.getArrivalMessage());
                }
            }
            else
            // 숫자조차 제공되지 않는 단계. 이 아래에서부터는 DB에 저장되어 있는 값을 통해 계산해야 함.
            {
                // "전역 뭐시기" State 일 때
                if (arrivalCode == TrainArrivalState.STATE_PRVSTTN_ENTERING.getStateCode()
                    || arrivalCode == TrainArrivalState.STATE_PRVSTTN_ARRIVED.getStateCode()
                        || arrivalCode == TrainArrivalState.STATE_PRVSTTN_DEPARTED.getStateCode() )
                {
                    // 일단 전역이 어디인지 알아내야 함.
                    SubwayStation prevStation = subwayStationService.getPreviousStation(pathHistory.getStartStation(), pathHistory.getEndStation());
                    // 걸리는 시간 계산.
                    realtimeRemainingSeconds = subwayStationService.calculateRemainingSeconds(prevStation, pathHistory.getEndStation());
                }
                // "진입중" 일 때
                else if( arrivalCode == TrainArrivalState.STATE_ENTERING.getStateCode())
                {
                    realtimeRemainingSeconds = 60; // 실제로 arrivalTime 이 유효해도 대충 이 정도로 반환해줌.
                }
                // "도착, 출발" 일 때
                else if(arrivalCode == TrainArrivalState.STATE_ARRIVED.getStateCode()
                        || arrivalCode == TrainArrivalState.STATE_DEPARTED.getStateCode())
                {
                    realtimeRemainingSeconds = 0; // 도착한거임. 0으로 반환.
                }
            }
        }
        return realtimeRemainingSeconds;
    }

    public void useReleaseSeatService(PathHistory pathHistory)
    {
        pathHistory.setExpectedArrivalTime(LocalDateTime.now());
        //TODO :: 자동하차 처리를 수행하게 되는데, 나중에 설정을 통해 자동으로 하차처리가 안 되게 해야 할 수도 있음.
        userTrainSeatService.releaseSeat(pathHistory.getUser().getId());
    }
}
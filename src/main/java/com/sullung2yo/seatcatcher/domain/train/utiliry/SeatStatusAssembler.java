package com.sullung2yo.seatcatcher.domain.train.utiliry;

import com.sullung2yo.seatcatcher.domain.path_history.service.PathHistoryService;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeat;
import com.sullung2yo.seatcatcher.domain.train.entity.UserTrainSeat;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatOccupant;
import com.sullung2yo.seatcatcher.domain.train.dto.response.SeatStatus;
import com.sullung2yo.seatcatcher.domain.train.repository.TrainSeatRepository;
import com.sullung2yo.seatcatcher.domain.train.repository.UserTrainSeatRepository;
import com.sullung2yo.seatcatcher.user.domain.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatStatusAssembler {

    private final TrainSeatRepository trainSeatRepository;
    private final UserTrainSeatRepository userTrainSeatRepository;
    private final PathHistoryService pathHistoryService;

    /**
     * 열차 좌석 정보, 사용자 정보 조합해서 응답 DTO 생성하는 메서드
     *
     * @param trainSeatGroup 좌석 정보를 조회할 열차 객체
     * @return 좌석 상태 정보가 업데이트된 SeatStatus List
     */
    public List<SeatStatus> assembleSeatResponse(@NonNull TrainSeatGroup trainSeatGroup) {
        // 1. 열차에 있는 모든 좌석 정보 조회 (Eager Load)
        List<TrainSeat> seats = trainSeatRepository.findAllWithTrain(trainSeatGroup);

        // 2. 점유자 정보 조회  <SeatId, User>
        Map<Long, User> occupants = userTrainSeatRepository
                .findAllByTrainSeat_IdIn(
                        seats.stream().map(TrainSeat::getId).toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        uts -> uts.getTrainSeat().getId(),
                        UserTrainSeat::getUser
                ));

        // 3. SeatStatus 모아서 반환
        return seats.stream()
                .map(seat -> SeatStatus.builder()
                        .seatId(seat.getId())
                        .seatLocation(seat.getSeatLocation())
                        .seatType(seat.getSeatType())
                        .occupant(occupants.containsKey(seat.getId())
                                ? SeatOccupant.builder() // // 점유자 정보가 존재하는 경우
                                .userId(occupants.get(seat.getId()).getId()) // 사용자 ID
                                .nickname(occupants.get(seat.getId()).getName()) // 사용자 닉네임
                                .expectedArrivalTime( // 도착역까지 남은 시간
                                        pathHistoryService.getUsersLatestPathHistory(
                                                occupants.get(seat.getId()).getId()
                                        ).getExpectedArrivalTime()
                                )
                                .getOffStationName( // 도착역 이름
                                        pathHistoryService.getUserDestination(occupants.get(seat.getId()))
                                                .orElse(null)
                                )
                                .profileImageNum(occupants.get(seat.getId()).getProfileImageNum()) // 프로필 이미지 번호
                                .tags(occupants.get(seat.getId()).getUserTag().stream().map(userTag -> userTag.getTag().getTagName()).toList()) // 태그
                                .build()
                                : null) // 점유자 정보가 존재하지 않는 경우 Null
                        .build())
                .collect(Collectors.toList());
    }
}

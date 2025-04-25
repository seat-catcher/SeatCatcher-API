package com.sullung2yo.seatcatcher.train.event_handler;

import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.event.SeatEvent;
import com.sullung2yo.seatcatcher.train.dto.event.SeatOccupant;
import com.sullung2yo.seatcatcher.train.dto.event.SeatStatus;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatRepository;
import com.sullung2yo.seatcatcher.train.repository.UserTrainSeatRepository;
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
public class SeatEventAssembler {

    private final TrainSeatRepository trainSeatRepository;
    private final UserTrainSeatRepository userTrainSeatRepository;

    public SeatEvent assembleSeatEvents(@NonNull Train train) {
        // 1. 열차에 있는 모든 좌석 정보 조회 (Eager Load)
        List<TrainSeat> seats = trainSeatRepository.findAllWithTrain(train);

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

        // 3. SeatStatus 목록 생성
        List<SeatStatus> seatStatuses = seats.stream()
                .map(seat -> SeatStatus.builder()
                        .seatId(seat.getId())
                        .seatLocation(seat.getSeatLocation())
                        .seatType(seat.getSeatType())
                        .occupant(occupants.containsKey(seat.getId())
                                ? SeatOccupant.builder() // // 점유자 정보가 존재하는 경우
                                    .userId(occupants.get(seat.getId()).getId())
                                    .nickname(occupants.get(seat.getId()).getName())
                                    .getOffRemainingCount(0) // TODO: ETA 계산 로직
                                    .build()
                                : null) // 점유자 정보가 존재하지 않는 경우 Null
                        .build())
                .collect(Collectors.toList());

        // 4. SeatEvent 조립 후 반환
        return SeatEvent.builder()
                .trainCode(train.getTrainCode())
                .carCode(train.getCarCode())
                .seatStatus(seatStatuses)
                .build();
    }
}

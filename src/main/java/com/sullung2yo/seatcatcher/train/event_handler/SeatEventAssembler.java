package com.sullung2yo.seatcatcher.train.event_handler;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SeatException;
import com.sullung2yo.seatcatcher.train.domain.SeatOccupationStatus;
import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.event.SeatEvent;
import com.sullung2yo.seatcatcher.train.dto.event.SeatOccupant;
import com.sullung2yo.seatcatcher.train.dto.event.SeatStatus;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import com.sullung2yo.seatcatcher.train.repository.UserTrainSeatRepository;
import com.sullung2yo.seatcatcher.user.domain.User;
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

    private final TrainRepository trainRepository;
    private final UserTrainSeatRepository userTrainSeatRepository;

    public SeatEvent assembleSeatEvents(Long seatId) {
        // 1. 좌석 ID로 해당 좌석이 속한 열차를 조회
        Train train = trainRepository.findTrainBySeatId(seatId)
                .orElseThrow(() -> new SeatException(
                        "seatId에 해당하는 열차 정보가 존재하지 않습니다.",
                        ErrorCode.TRAIN_NOT_FOUND));

        // 2. 열차에 있는 모든 좌석 정보 조회
        List<TrainSeat> seats = train.getTrainSeats();

        // 3. 점유자 정보 조회  <SeatId, User>
        Map<Long, User> occupants = userTrainSeatRepository
                .findAllByTrainSeatIdIn(
                        seats.stream().map(TrainSeat::getId).toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        uts -> uts.getTrainSeat().getId(),
                        UserTrainSeat::getUser
                ));

        // 4. SeatStatus 목록 생성
        List<SeatStatus> seatStatuses = seats.stream()
                .map(seat -> SeatStatus.builder()
                        .seatId(seat.getId())
                        .seatLocation(seat.getSeatLocation())
                        .seatType(seat.getSeatType())
                        .isOccupied(occupants.containsKey(seat.getId()) // seat.getId()에 해당하는 점유자 키로 점유 여부 조사
                                ? SeatOccupationStatus.OCCUPIED // 점유자가 존재하는 경우
                                : SeatOccupationStatus.EMPTY) // 점유자가 존재하지 않는 경우
                        .occupant(occupants.containsKey(seat.getId())
                                ? SeatOccupant.builder() // // 점유자 정보가 존재하는 경우
                                    .userId(occupants.get(seat.getId()).getId())
                                    .nickname(occupants.get(seat.getId()).getName())
                                    .getOffRemainingCount(0) // TODO: ETA 계산 로직
                                    .build()
                                : null) // 점유자 정보가 존재하지 않는 경우 Null
                        .build())
                .collect(Collectors.toList());

        // 5. SeatEvent 조립 후 반환
        return SeatEvent.builder()
                .trainCode(train.getTrainCode())
                .carCode(train.getCarCode())
                .seatStatus(seatStatuses)
                .build();
    }
}

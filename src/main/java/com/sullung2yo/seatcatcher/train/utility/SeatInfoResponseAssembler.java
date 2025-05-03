package com.sullung2yo.seatcatcher.train.utility;

import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.response.SeatInfoResponse;
import com.sullung2yo.seatcatcher.train.dto.response.SeatOccupant;
import com.sullung2yo.seatcatcher.train.dto.response.SeatStatus;
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
public class SeatInfoResponseAssembler {

    private final TrainSeatRepository trainSeatRepository;
    private final UserTrainSeatRepository userTrainSeatRepository;

    /**
     * 열차 좌석 정보, 사용자 정보 조합해서 응답 DTO 생성하는 메서드
     *
     * @param train 좌석 정보를 조회할 열차 객체
     * @return 좌석 상태 정보가 포함된 업데이트된 SeatInfoResponse 객체
     */
    public SeatInfoResponse assembleSeatResponse(@NonNull Train train) {
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

        // 4. SeatInfoResponse 조립 후 반환
        return SeatInfoResponse.builder()
                .trainCode(train.getTrainCode())
                .carCode(train.getCarCode())
                .seatStatus(seatStatuses)
                .build();
    }
}

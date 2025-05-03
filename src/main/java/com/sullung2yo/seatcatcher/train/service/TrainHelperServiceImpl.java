package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatGroupType;
import com.sullung2yo.seatcatcher.train.domain.SeatType;
import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // 이걸 추가하면 로깅을 마음껏 쓸 수 있음.
public class TrainHelperServiceImpl implements TrainHelperService {

    private final TrainRepository trainRepository;

    @Override
    @Transactional
    public List<Train> createGroupsOf(String trainCode, String carCode) {
        List<Train> groups = new ArrayList<>();

        // trainCode 를 통해 매핑을 해서 어떤 타입의 좌석을 만들어야 하는지를 알아내야 함.

        // 기본적인 37773 배치의 차량을 세팅하는 코드.
        groups.add(create(trainCode, carCode, SeatGroupType.ELDERLY_A));
        groups.add(create(trainCode, carCode, SeatGroupType.NORMAL_A_14));
        groups.add(create(trainCode, carCode, SeatGroupType.NORMAL_B_14));
        groups.add(create(trainCode, carCode, SeatGroupType.NORMAL_C_14));
        groups.add(create(trainCode, carCode, SeatGroupType.ELDERLY_B));

/*
        groups.add(create(trainCode, carCode, SeatGroupType.ELDERLY_A));
        groups.add(create(trainCode, carCode, SeatGroupType.NORMAL_A_12));
        groups.add(create(trainCode, carCode, SeatGroupType.NORMAL_B_12));
        groups.add(create(trainCode, carCode, SeatGroupType.NORMAL_C_12));
        groups.add(create(trainCode, carCode, SeatGroupType.ELDERLY_B));
 */

        trainRepository.saveAll(groups);

        return groups;
    }

    @Override
    public Train create(String trainCode, String carCode, SeatGroupType groupType){

        Train train = Train.builder()
                .trainCode(trainCode)
                .carCode(carCode)
                .trainSeat(new ArrayList<>())
                .type(groupType)
                .build();

        for(int i = 0; i < groupType.getSeatCount(); i++)
        {
            SeatType seatType;
            if(train.getType() == SeatGroupType.ELDERLY_A || train.getType() == SeatGroupType.ELDERLY_B)
            {
                seatType = SeatType.ELDERLY;
            }
            else seatType = SeatType.NORMAL; // 임산부 좌석은 고려하지 않고 일단 Normal 로 모두 설정하겠음. TODO :: 추후에 임산부 좌석이 고려되어야 할 경우 이 부분을 변경할 것.

            TrainSeat trainSeat = TrainSeat.builder()
                    .train(train)
                    .seatLocation(i)
                    .seatType(seatType)
                    .build();
            train.getTrainSeat().add(trainSeat);
        }

        return train;
    }
}

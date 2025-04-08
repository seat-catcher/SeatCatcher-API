package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.*;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // 이걸 추가하면 로깅을 마음껏 쓸 수 있음.
public class TrainSeatGroupServiceImpl implements TrainSeatGroupService {

    private final TrainSeatGroupRepository trainSeatGroupRepository;

    @Override
    public TrainSeatGroup create(TrainCar car, SeatGroupType groupType){

        TrainSeatGroup trainSeatGroup = TrainSeatGroup.builder()
                .trainCar(car)
                .trainSeats(new ArrayList<>())
                .type(groupType)
                .build();

        for(int i = 0; i < groupType.getSeatCount(); i++)
        {
            SeatType seatType;
            if(trainSeatGroup.getType() == SeatGroupType.ELDERLY_A || trainSeatGroup.getType() == SeatGroupType.ELDERLY_B)
            {
                seatType = SeatType.ELDERLY;
            }
            else seatType = SeatType.NORMAL; // 임산부 좌석은 고려하지 않고 일단 Normal 로 모두 설정하겠음. TODO :: 추후에 임산부 좌석이 고려되어야 할 경우 이 부분을 변경할 것.

            TrainSeat trainSeat = TrainSeat.builder()
                    .trainSeatGroup(trainSeatGroup)
                    .seatLocation(i)
                    .seatType(seatType)
                    .jjimCount(0)
                    .build();
            trainSeatGroup.getTrainSeats().add(trainSeat);
        }

        /*
            trainSeatGroupRepository.save(trainSeatGroup);
            trainSeatRepository.saveAll(trainSeats);

            이 부분은 최종적으로 Train 이 저장될 때 Cascade 에 의해 자동으로 이루어진다. 지금 저장하면 불필요한 중복 저장이 발생!
        */

        return trainSeatGroup;
    }

    @Override
    public List<TrainSeatGroup> findAllByTrainCarId(Long carId) {
        return trainSeatGroupRepository.findAllByTrainCarId(carId);
    }

    @Override
    public TrainSeatGroup findBySeatGroupId(Long seatGroupId) {
        return trainSeatGroupRepository.findById(seatGroupId).orElse(null);
        // 일단 만들어놓긴 했는데 우선순위가 낮아서 사용하진 않음.
    }

    @Override
    public void update(TrainSeatGroup trainSeatGroup) {
        // 우선순위가 높지 않으므로 일단 미구현
    }

    @Override
    public void delete(TrainSeatGroup trainSeatGroup) {
        // 우선순위가 높지 않으므로 일단 미구현
    }
}

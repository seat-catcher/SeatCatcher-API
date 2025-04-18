package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.*;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // 이걸 추가하면 로깅을 마음껏 쓸 수 있음.
public class TrainSeatGroupServiceImpl implements TrainSeatGroupService {

    private final TrainSeatGroupRepository trainSeatGroupRepository;


    @Override
    public List<TrainSeatGroup> findByTrainCodeAndCarCode(String trainCode, String carCode) {
        List<TrainSeatGroup> result = trainSeatGroupRepository.findAllByTrainCodeAndCarCode(trainCode, carCode);

        if(result.isEmpty())
        {
            throw new EntityNotFoundException("TrainSeatGroup not found"); // 이를 Catch 하면 됨.
        }

        else return result;
    }

    @Override
    @Transactional
    public List<TrainSeatGroup> createGroupsOf(String trainCode, String carCode) {
        List<TrainSeatGroup> groups = new ArrayList<>();

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

        trainSeatGroupRepository.saveAll(groups);

        return groups;
    }

    @Override
    @Transactional
    public TrainSeatGroup create(String trainCode, String carCode, SeatGroupType groupType){

        TrainSeatGroup trainSeatGroup = TrainSeatGroup.builder()
                .trainCode(trainCode)
                .carCode(carCode)
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

        return trainSeatGroup;
    }
}





/*
    // Old ::TODO 변경 사항 테스트가 확정되면 해당 주석을 제거해야 합니다.

        @Override
        public List<TrainSeatGroup> findAllByTrainCarId(Long carId) {
        return trainSeatGroupRepository.findAllByTrainCarId(carId);
        }

        @Override
        public TrainSeatGroup findBySeatGroupId(Long seatGroupId) {
        return trainSeatGroupRepository.findById(seatGroupId)
                .orElseThrow(() -> new EntityNotFoundException("SeatGroup not found with id: " + seatGroupId));
        // 일단 만들어놓긴 했는데 우선순위가 낮아서 사용하진 않음.
        }

        @Override
        @Transactional
        public void update(TrainSeatGroup trainSeatGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
        // 우선순위가 높지 않으므로 일단 미구현
        }

        @Override
        @Transactional
        public void delete(TrainSeatGroup trainSeatGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
        // 우선순위가 높지 않으므로 일단 미구현
        }

 */

package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.*;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // 이걸 추가하면 로깅을 마음껏 쓸 수 있음.
public class TrainSeatGroupServiceImpl implements TrainSeatGroupService {

    private final TrainRepository trainRepository;
    private final ApplicationContext applicationContext;

    @Override
    public List<Train> findByTrainCodeAndCarCode(String trainCode, String carCode) {
        List<Train> result = trainRepository.findAllByTrainCodeAndCarCode(trainCode, carCode);

        if(result.isEmpty())
        {
            throw new EntityNotFoundException("TrainSeatGroup not found"); // 이를 Catch 하면 됨.
        }

        else return result;
    }

    /*
        열차 번호와 차량 번호로 좌석 그룹을 찾고
        만약 없다면 새로 생성까지 하는 함수입니다.

        해당 함수에서 호출하는 createGroupsOf 함수는
        Database 에 commit 하는 기능을 포함합니다. 따라서 별도의 save가 필요 없습니다.
     */
    @Override
    public List<Train> findOrCreateByTrainCodeAndCarCode(String trainCode, String carCode) {
        try
        {
            return findByTrainCodeAndCarCode(trainCode, carCode);
        }
        catch(EntityNotFoundException e)
        {
            TrainSeatGroupService proxy = (TrainSeatGroupService) applicationContext.getBean("trainSeatGroupService");
            // 해당 함수에서 Service 의 Transactional 이 붙은 함수를 호출하고 있으므로, 프록시 객체를 만들어서 해당 프록시를 통해 트랜잭션을 보장.
            return proxy.createGroupsOf(trainCode, carCode);
        }
    }

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
    @Transactional
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

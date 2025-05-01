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
    private final TrainSeatGroupHelperService helperService;

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
        catch(EntityNotFoundException e) {
            try
            {
                // 이렇게 하면 동시성 문제를 해결할 수 있다고 함.
                return findByTrainCodeAndCarCode(trainCode, carCode);
            }
            catch (EntityNotFoundException secondAttemptException)
            {
                log.info("TrainSeatGroup not found for trainCode: {} and carCode: {}, creating new groups", trainCode, carCode);
                return createGroupsOf(trainCode, carCode);
            }
        }
    }

    @Override
    public List<Train> createGroupsOf(String trainCode, String carCode) {
        return helperService.createGroupsOf(trainCode, carCode);
    }

    @Override
    public Train create(String trainCode, String carCode, SeatGroupType groupType){
        return helperService.create(trainCode, carCode, groupType);
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

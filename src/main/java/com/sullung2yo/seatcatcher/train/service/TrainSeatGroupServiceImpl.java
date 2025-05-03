package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.*;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // 이걸 추가하면 로깅을 마음껏 쓸 수 있음.
public class TrainSeatGroupServiceImpl implements TrainSeatGroupService {

    private final TrainRepository trainRepository;
    private final TrainHelperService helperService;

    @Override
    public List<TrainSeatGroup> findAllByTrainCode(String trainCode, String carCode) {
        return trainRepository.findAllByTrainCode(trainCode);
    }

    /*
        열차 번호와 차량 번호로 좌석 그룹을 찾고
        만약 없다면 새로 생성까지 하는 함수입니다.

        해당 함수에서 호출하는 createGroupsOf 함수는
        Database 에 commit 하는 기능을 포함합니다. 따라서 별도의 save가 필요 없습니다.
     */
    @Override
    @Transactional
    public List<TrainSeatGroup> findOrCreateByTrainCode(@NonNull String trainCode, @NonNull String carCode) {
        // trainCode 를 통해 매핑을 해서 어떤 타입의 좌석을 만들어야 하는지를 알아내야 함.
    }

    @Override
    public List<TrainSeatGroup> createGroupsOf(String trainCode, String carCode) {
        return helperService.createGroupsOf(trainCode, carCode);
    }

    @Override
    public TrainSeatGroup create(String trainCode, String carCode, SeatGroupType groupType){
        return helperService.create(trainCode, carCode, groupType);
    }

}

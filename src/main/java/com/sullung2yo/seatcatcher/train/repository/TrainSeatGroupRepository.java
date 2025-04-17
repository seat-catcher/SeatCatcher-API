package com.sullung2yo.seatcatcher.train.repository;

import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainSeatGroupRepository extends JpaRepository<TrainSeatGroup, Long> {
    List<TrainSeatGroup> findAllByTrainCodeAndCarCode(String trainCode, String carCode);
}

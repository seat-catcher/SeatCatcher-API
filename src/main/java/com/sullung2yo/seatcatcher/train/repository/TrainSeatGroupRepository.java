package com.sullung2yo.seatcatcher.train.repository;

import com.sullung2yo.seatcatcher.train.domain.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainSeatGroupRepository extends JpaRepository<Train, Long> {
    List<Train> findAllByTrainCodeAndCarCode(String trainCode, String carCode);
}

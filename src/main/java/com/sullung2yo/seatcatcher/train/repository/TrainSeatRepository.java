package com.sullung2yo.seatcatcher.train.repository;

import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainSeatRepository extends JpaRepository<TrainSeat, Long> {
    List<TrainSeat> findAllByTrainSeatGroupId(Long id);
}

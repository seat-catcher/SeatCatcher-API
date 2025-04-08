package com.sullung2yo.seatcatcher.train.repository;

import com.sullung2yo.seatcatcher.train.domain.TrainCar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainCarRepository extends JpaRepository<TrainCar, Long> {
}

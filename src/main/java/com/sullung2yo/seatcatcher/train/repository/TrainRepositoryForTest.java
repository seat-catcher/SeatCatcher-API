package com.sullung2yo.seatcatcher.train.repository;

import com.sullung2yo.seatcatcher.train.domain.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepositoryForTest extends JpaRepository<Train, Long> {

}

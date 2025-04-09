package com.sullung2yo.seatcatcher.train.repository;

import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTrainSeatRepository extends JpaRepository<UserTrainSeat, Long> {
    Optional<UserTrainSeat> findUserTrainSeatByUserId(Long id);
    Optional<UserTrainSeat> findUserTrainSeatBySeatId(Long id);
}

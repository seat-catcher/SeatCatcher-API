package com.sullung2yo.seatcatcher.domain.train.repository;

import com.sullung2yo.seatcatcher.domain.train.entity.UserTrainSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTrainSeatRepository extends JpaRepository<UserTrainSeat, Long> {
    Optional<UserTrainSeat> findUserTrainSeatByUserId(Long id);
    Optional<UserTrainSeat> findUserTrainSeatByTrainSeatId(Long id);
    void deleteUserTrainSeatByUserId(Long userId);

    // 좌성 id 리스트를 사용해서 UserTrainSeat 리스트를 가져오는 메서드
    // In을 붙이면 In 쿼리로 변환됨 (SELECT * FROM a WHERE id IN 리스트)
    List<UserTrainSeat> findAllByTrainSeat_IdIn(List<Long> trainSeatIds);
}

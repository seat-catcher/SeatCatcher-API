package com.sullung2yo.seatcatcher.domain.train.repository;

import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainSeatGroupRepository extends JpaRepository<TrainSeatGroup, Long> {
    List<TrainSeatGroup> findAllByTrainCodeAndCarCode(String trainCode, String carCode);

    /**
     * 좌석 ID로 해당 좌석이 속한 열차를 조회하는 메서드
     * JPQL을 사용해 엔티티 이름과 매핑 컬럼을 그대로 활용
     */
    @Query("select ts.trainSeatGroup from TrainSeat ts where ts.id = :seatId")
    Optional<TrainSeatGroup> findTrainBySeatId(@Param("seatId") Long seatId);

}

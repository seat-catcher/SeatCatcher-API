package com.sullung2yo.seatcatcher.train.repository;

import com.sullung2yo.seatcatcher.train.domain.TrainSeatGroup;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainSeatRepository extends JpaRepository<TrainSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락을 사용해서, 해당 TrainSeat을 비관적 락이 걸린채로 가져옴 -> (화장실 변기를 사용할 수 있음) // 다른 쓰레드는 해당 TrainSeat을 수정할 수 없음 (화장실 사용 불가)
    @Query("SELECT ts FROM TrainSeat ts WHERE ts.id = :seatId")
    Optional<TrainSeat> findByIdForUpdate(@Param("seatId") Long seatId);

    // JOIN FETCH는 일반적인 SQL에는 없는데,
    // JPQL에서 즉시 로딩해서 조인된 데이터 가져올 때 사용한다고 합니다.
    // Train의 trainSeats가 Lazy로딩으로 설정되어 있어서
    // Eager 로딩으로 사용해야 N+1 쿼리 문제가 발생하지 않기 때문에
    // JPQL에서 JOIN FETCH를 사용해야 합니다.
    @Query("SELECT DISTINCT ts FROM TrainSeat AS ts JOIN FETCH ts.train as t WHERE t = :train")
    List<TrainSeat> findAllWithTrain(TrainSeatGroup trainSeatGroup);

    @Query("SELECT t FROM TrainSeat ts JOIN ts.train AS t WHERE ts.id = :seatId")
    Optional<TrainSeatGroup> findTrainByTrainSeatId(Long seatId);
}

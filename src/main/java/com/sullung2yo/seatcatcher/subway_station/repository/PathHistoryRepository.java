package com.sullung2yo.seatcatcher.subway_station.repository;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PathHistoryRepository extends JpaRepository<PathHistory, Long> {

}

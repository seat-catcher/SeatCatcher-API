package com.sullung2yo.seatcatcher.subway_station.repository;

import com.sullung2yo.seatcatcher.subway_station.domain.SubwayLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubwayLineRepository extends JpaRepository<SubwayLine, Long> {
    SubwayLine findByLineName(String lineName);
    boolean existsByLineName(String lineName);
}

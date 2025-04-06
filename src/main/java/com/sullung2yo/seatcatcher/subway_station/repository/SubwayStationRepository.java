package com.sullung2yo.seatcatcher.subway_station.repository;

import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Long> {
}
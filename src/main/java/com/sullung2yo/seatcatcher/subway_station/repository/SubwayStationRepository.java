package com.sullung2yo.seatcatcher.subway_station.repository;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Long> {
    public List<SubwayStation> findByStationNameContaining(String name);

    @Query(
            "SELECT s FROM SubwayStation s " +
                    "WHERE (:keyword IS NULL OR s.stationName LIKE CONCAT('%', :keyword, '%')) " +
                    "AND (:line IS NULL OR s.line = :line) " +
                    "ORDER BY " +
                    "CASE WHEN :order = 'up' THEN s.accumulateDistance " +
                        "WHEN :order = 'down' THEN -s.accumulateDistance " +
                        "ELSE s.accumulateDistance END ASC"
    )
    List<SubwayStation> findBy(@Param("keyword") String keyword, @Param("line") Line line, @Param("order") String order);
}
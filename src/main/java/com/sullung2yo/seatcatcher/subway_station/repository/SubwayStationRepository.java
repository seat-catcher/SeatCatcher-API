package com.sullung2yo.seatcatcher.subway_station.repository;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Long> {
    List<SubwayStation> findByStationNameContaining(String name);

    SubwayStation findByStationNameAndLine(String stationName, Line line);

    @Query(
            "SELECT s FROM SubwayStation s " +
                    "WHERE (:keyword IS NULL OR s.stationName LIKE CONCAT('%', :keyword, '%')) " + //TODO :: Query DSL 검색해보면 Improve 힌트가 될 것.
                    "AND (:line IS NULL OR s.line = :line) " +
                    "ORDER BY " +
                    "CASE WHEN :order = 'up' THEN s.accumulateDistance " +
                        "WHEN :order = 'down' THEN -s.accumulateDistance " +
                        "ELSE s.accumulateDistance END ASC"
    )
    List<SubwayStation> findBy(@Param("keyword") String keyword, @Param("line") Line line, @Param("order") String order);
}
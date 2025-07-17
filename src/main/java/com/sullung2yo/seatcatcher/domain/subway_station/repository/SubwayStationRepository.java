package com.sullung2yo.seatcatcher.domain.subway_station.repository;

import com.sullung2yo.seatcatcher.domain.subway_station.enums.Line;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    // 상행선 방향에서 현재 역의 accumulateDistance 를 통해 이전 역이 어떤 역인지를 알아냄.
    Optional<SubwayStation> findTopByLineAndAccumulateDistanceGreaterThanOrderByAccumulateDistanceAsc(Line line, float accumulateDistance);

    // 하행선 방향에서 현재 역의 accumulateDistance 를 통해 이전 역이 어떤 역인지를 알아냄.
    Optional<SubwayStation> findTopByLineAndAccumulateDistanceLessThanOrderByAccumulateDistanceDesc(Line line, float accumulateDistance);
}
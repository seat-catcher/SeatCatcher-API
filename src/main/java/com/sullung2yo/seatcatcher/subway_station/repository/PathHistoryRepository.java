package com.sullung2yo.seatcatcher.subway_station.repository;

import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathHistoryRepository extends JpaRepository<PathHistory, Long> {
    @Query("""
    SELECT ph
    FROM PathHistory ph
    WHERE ph.user = :user
      AND (:lastPathId IS NULL OR ph.id < :lastPathId)
    ORDER BY ph.id DESC
""")
    List<PathHistory> findScrollByUserAndCursor(
            @Param("user") User user,
            @Param("lastPathId") Long lastPathId,
            Pageable pageable
    );

    @Query("SELECT ph.endStation FROM PathHistory ph WHERE ph.user = :user")
    SubwayStation findEndStationByUser(User user);
}
